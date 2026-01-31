package com.tunfin.payment.service;

import com.tunfin.payment.client.WalletClient;
import com.tunfin.payment.dto.WalletDto;
import com.tunfin.payment.model.Payment;
import com.tunfin.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class PaymentService {

        private final PaymentRepository paymentRepository;
        private final WalletClient walletClient;
        private final com.tunfin.payment.client.IdentityClient identityClient;
        private final EmailService emailService;
        private final NotificationService notificationService;

        public Payment processP2PTransfer(UUID senderAccountId, UUID receiverAccountId, BigDecimal amount) {
                // 1. Fetch Account Details to get User IDs
                var senderAccount = walletClient.getAccount(senderAccountId);
                var receiverAccount = walletClient.getAccount(receiverAccountId);

                // 2. Fetch User Details to get Emails and KYC status
                var senderUser = identityClient.getUserById(UUID.fromString(senderAccount.getUserId()));
                var receiverUser = identityClient.getUserById(UUID.fromString(receiverAccount.getUserId()));

                // 3. Enforce KYC for Sender and Receiver
                if (!"VERIFIED".equals(senderUser.get("kycLevel"))) {
                        log.error(">>> P2P Denied: Sender {} is not VERIFIED. Current level: {}",
                                        senderUser.get("fullName"), senderUser.get("kycLevel"));
                        throw new RuntimeException(
                                        "KYC Verification Required for P2P Transfers. Please verify your identity first.");
                }

                if (!"VERIFIED".equals(receiverUser.get("kycLevel"))) {
                        log.error(">>> P2P Denied: Receiver {} is not VERIFIED. Current level: {}",
                                        receiverUser.get("fullName"), receiverUser.get("kycLevel"));
                        throw new RuntimeException(
                                        "Recipient is not KYC verified. They must verify their identity to receive funds.");
                }

                // 4. Record Transaction in Wallet Service
                var req = com.tunfin.payment.dto.WalletDto.TransactionRequest.builder()
                                .referenceId(UUID.randomUUID().toString())
                                .type("P2P_TRANSFER")
                                .description("Transfer from " + senderUser.get("fullName") + " to "
                                                + receiverUser.get("fullName"))
                                .entries(List.of(
                                                new com.tunfin.payment.dto.WalletDto.LedgerEntryRequest(senderAccountId,
                                                                amount), // Debit
                                                new com.tunfin.payment.dto.WalletDto.LedgerEntryRequest(
                                                                receiverAccountId, amount.negate()) // Credit
                                ))
                                .build();

                walletClient.recordTransaction(req);

                // 4. Save Payment Record
                var payment = Payment.builder()
                                .senderId(senderAccountId)
                                .receiverId(receiverAccountId)
                                .amount(amount)
                                .status("SUCCESS")
                                .build();
                payment = paymentRepository.save(payment);

                // 5. Send Notifications
                try {
                        emailService.sendTransferNotification(
                                        senderUser.get("email"),
                                        senderUser.get("fullName"),
                                        "SENDER",
                                        amount,
                                        receiverUser.get("fullName"));

                        emailService.sendTransferNotification(
                                        receiverUser.get("email"),
                                        receiverUser.get("fullName"),
                                        "RECEIVER",
                                        amount,
                                        senderUser.get("fullName"));

                        // Push Notifications
                        notificationService.sendPushNotification(
                                        senderUser.get("fcmToken"),
                                        "Transfer Sent",
                                        "You sent " + amount + " TND to " + receiverUser.get("fullName"));

                        notificationService.sendPushNotification(
                                        receiverUser.get("fcmToken"),
                                        "Transfer Received",
                                        "You received " + amount + " TND from " + senderUser.get("fullName"));
                } catch (Exception e) {
                        // Log but don't fail the transaction if email/push fails
                        System.err.println(">>> FAILED to send notifications: " + e.getMessage());
                }

                return payment;
        }
}
