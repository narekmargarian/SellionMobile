package com.sellion.mobile.managers;

import com.sellion.mobile.entity.ReturnModel;

import java.util.ArrayList;
import java.util.List;

public class ReturnHistoryManager {
    private static ReturnHistoryManager instance;
    private final List<ReturnModel> returns = new ArrayList<>();

    private ReturnHistoryManager() {}

    public static synchronized ReturnHistoryManager getInstance() {
        if (instance == null) instance = new ReturnHistoryManager();
        return instance;
    }

    public void addReturn(ReturnModel newReturn) {
        // ИСПРАВЛЕНО: Удаляем старую запись только если она еще НЕ отправлена (черновик)
        // Это позволяет создавать новый возврат для того же магазина после синхронизации
        returns.removeIf(r -> r.shopName.equals(newReturn.shopName) && r.status == ReturnModel.Status.PENDING);
        returns.add(newReturn);
    }

    public List<ReturnModel> getReturns() {
        return returns;
    }

    // ИСПРАВЛЕНО: Поиск с конца списка для нахождения самого НОВОГО возврата
    public ReturnModel getReturn(String shopName) {
        for (int i = returns.size() - 1; i >= 0; i--) {
            ReturnModel r = returns.get(i);
            if (r.shopName.equals(shopName)) return r;
        }
        return null;
    }
}

