package com.fintech.wallet.service.impl;

import com.fintech.common.grpc.BalanceCheckRequest;
import com.fintech.common.grpc.BalanceCheckResponse;
import com.fintech.common.grpc.WalletServiceGrpc;
import com.fintech.wallet.model.Wallet;
import com.fintech.wallet.repository.WalletRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class WalletGrpcServiceImpl extends WalletServiceGrpc.WalletServiceImplBase {

    private final WalletRepository walletRepository;

    @Override
    public void checkBalance(BalanceCheckRequest request, StreamObserver<BalanceCheckResponse> responseObserver) {
        log.info("gRPC Request received: UserId={}, Amount={}", request.getUserId(), request.getAmount());

        Wallet wallet = walletRepository.findByUserIdAndCurrency(request.getUserId(), request.getCurrency())
                .orElse(null);

        boolean sufficient = false;
        String msg = "Wallet not found";

        if (wallet != null) {
            BigDecimal requestedAmount = BigDecimal.valueOf(request.getAmount());
            if (wallet.getBalance().compareTo(requestedAmount) >= 0) {
                sufficient = true;
                msg = "Funds available";
            } else {
                msg = "Insufficient funds";
                log.warn("gRPC Check Failed: Insufficient funds for User {}", request.getUserId());
            }
        }

        BalanceCheckResponse response = BalanceCheckResponse.newBuilder()
                .setHasSufficientFunds(sufficient)
                .setMessage(msg)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}