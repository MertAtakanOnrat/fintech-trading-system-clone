package com.fintech.order.domain.enums;

public enum OrderStatus {
    PENDING,            // 1. İlk kayıt (Cüzdan kontrolü bekleniyor)
    VALIDATED,          // 2. Cüzdan onayı geldi (Bakiye bloke edildi)
    REJECTED,           // 3. Yetersiz bakiye (Saga iptali)
    MATCHED,            // 4. Borsa motorunda eşleşti (Trade gerçekleşti)
    CANCELLED           // 5. Kullanıcı iptal etti
}
