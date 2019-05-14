package com.dlm.slidebarcityselectdemo.util;

import java.text.CollationKey;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Locale;

/**
 * @Author
 * @Time 2018/12/18 10:20
 * @Describe 处理重庆问题（重庆会被分析为zhongqin）
 * @Modify
 */
public class CityComparator implements Comparator<String> {

    private RuleBasedCollator collator;

    public CityComparator() {

        collator = (RuleBasedCollator) Collator.getInstance(Locale.CHINA);

    }

    @Override
    public int compare(String leftHs, String rightHs) {

        leftHs = leftHs.replace("重庆", "崇庆");
        rightHs = rightHs.replace("重庆", "崇庆");
        CollationKey c1 = collator.getCollationKey(leftHs);
        CollationKey c2 = collator.getCollationKey(rightHs);

        return c1.compareTo(c2);
    }


}
