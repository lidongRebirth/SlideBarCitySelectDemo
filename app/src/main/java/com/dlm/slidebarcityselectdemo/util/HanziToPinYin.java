package com.dlm.slidebarcityselectdemo.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * @Author      LD
 * @Time        2018/12/18 10:15
 * @Describe    汉字转为拼音的类，返回首字母
 * @Modify
 */
public class HanziToPinYin {
    /**
     * 如果字符串string是汉字，则转为拼音并返回,返回的是首字母
     * @param string
     * @return
     */
    public static char toPinYin(String string){
        HanyuPinyinOutputFormat hanyuPinyin = new HanyuPinyinOutputFormat();
        hanyuPinyin.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        hanyuPinyin.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        hanyuPinyin.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
        String[] pinyinArray=null;
        char hanzi = string.charAt(0);
        try {
            //是否在汉字范围内
            if(hanzi>=0x4e00 && hanzi<=0x9fa5){
                pinyinArray = PinyinHelper.toHanyuPinyinStringArray(hanzi, hanyuPinyin);
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        //将获取到的拼音返回，只返回其首字母
        return pinyinArray[0].charAt(0);
    }
}
