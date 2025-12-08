package com.fintech.order.service;

import com.fintech.common.grpc.BalanceCheckRequest;
import com.fintech.common.grpc.BalanceCheckResponse;
import com.fintech.common.grpc.WalletServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WalletGrpcClient {

    @GrpcClient("wallet-service")
    private WalletServiceGrpc.WalletServiceBlockingStub walletStub;

    public boolean hasSufficientFunds(Long userId, double amount) {
        try {
            BalanceCheckRequest request = BalanceCheckRequest.newBuilder()
                    .setUserId(userId)
                    .setCurrency("TRY")
                    .setAmount(amount)
                    .build();

            BalanceCheckResponse response = walletStub.checkBalance(request);
            return response.getHasSufficientFunds();
        } catch (Exception e) {
            log.error("gRPC call failed", e);
            return false;
        }
    }
}