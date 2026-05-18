package service;

import domain.MenuItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MenuManagementService {
    private Map<String, MenuItem> menuDatabase = new HashMap<>();

    public void addMenuItem(MenuItem item) {
        menuDatabase.put(item.getMenuItemID(), item);
    }
    public void removeMenuItem(String itemId) {
            menuDatabase.remove(itemId);
    }
    public void updatePrice(String itemId, double price){
        MenuItem item = menuDatabase.get(itemId);
        if(item != null){
            item.setPrice(price);
        }
    }
    public Collection<MenuItem> getAllMenuItems() {
        return menuDatabase.values();
    }
    public MenuItem getMenuItem(String itemId) {
        return menuDatabase.get(itemId);
    }
}
