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
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WalletClient walletClient;

    public Payment processP2PTransfer(UUID senderAccountId, UUID receiverAccountId, BigDecimal amount) {
        // 1. Create Transaction Request for Wallet Service
        // Credit Sender (-), Debit Receiver (+) ?? No.
        // Sender (Liability) loses money -> Debit (+)?
        // Wait, Liability Account:
        // Debit (+) = Decrease Balance (Owe Less)
        // Credit (-) = Increase Balance (Owe More) -> Deposit

        // P2P:
        // Sender (Liability) Balance decreases -> Debit (+)
        // Receiver (Liability) Balance increases -> Credit (-)

        // Wait, standard accounting:
        // Assets = Liabilities + Equity
        // Cash (Asset) Debit (+), Credit (-)
        // User Wallet (Liability from Bank perspective):
        // We (Bank) owe user money.
        // User spends money -> We owe less -> Debit Liability.
        // User receives money -> We owe more -> Credit Liability.

        // Correct logic:
        // Sender: Debit (+) amount
        // Receiver: Credit (-) amount
        // Sum = 0.

        var req = WalletDto.TransactionRequest.builder()
                .referenceId(UUID.randomUUID().toString())
                .type("P2P_TRANSFER")
                .description("Transfer from " + senderAccountId)
                .entries(List.of(
                        new WalletDto.LedgerEntryRequest(senderAccountId, amount), // Debit
                        new WalletDto.LedgerEntryRequest(receiverAccountId, amount.negate()) // Credit
                ))
                .build();

        walletClient.recordTransaction(req);

        var payment = Payment.builder()
                .senderId(senderAccountId) // Actually storing Account ID here for now
                .receiverId(receiverAccountId)
                .amount(amount)
                .status("SUCCESS")
                .build();

        return paymentRepository.save(payment);
    }
}
