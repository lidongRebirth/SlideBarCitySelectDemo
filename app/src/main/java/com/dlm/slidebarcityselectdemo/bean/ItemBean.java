package com.dlm.slidebarcityselectdemo.bean;

/**
 * @Author      LD
 * @Time        2018/12/18 14:23
 * @Describe    item的类，根据type来区分是字母还是城市名
 * @Modify
 */
public class ItemBean {

    private String itemName;    //item的名字,若为汉字，则写汉字，若无A等，则写A
    private String itemType;    //类型，区分是首字母还是实际的名字（城市名字）A-Z的话是label,不是的话为汉字的拼音首字母

    // 标记  拼音头，label为0
    public static final int TYPE_HEAD = 0;
    // 标记  城市名
    public static final int TYPE_CITY = 1;

    public int getType() {

        if (itemType.equals("head")) {
            return TYPE_HEAD;
        } else {
            return TYPE_CITY;
        }
    }
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

}
