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



    // РАБОТА С ВОЗВРАТАМИ
    public void addReturn(ReturnModel returnModel) {
        returns.removeIf(r -> r.shopName.equals(returnModel.shopName));
        returns.add(returnModel);
    }

    public List<ReturnModel> getReturns() {
        return returns;
    }

    public ReturnModel getReturn(String shopName) {
        for (ReturnModel r : returns) {
            if (r.shopName.equals(shopName)) return r;
        }
        return null;
    }
}

