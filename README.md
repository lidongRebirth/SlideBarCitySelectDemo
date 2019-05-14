
### 涉及到的内容：

1.  首先会ListView或RecyclerView的多布局。

2.  自定义View右侧拼音列表，简单地绘制并设立监听事件等。

3.  会使用[pinyin4.jar](http://pinyin4j.sourceforge.net/pinyin4j-doc/)第三方包来识别汉字的首字母（单独处理重庆多音问题）。

4.  将全部的城市列表转化为{A a开头城市名...，B b开头城市名...}的格式，这个数据转化是重点**！！！**

5.  将第三步获取的数据来多布局展示出来。

## 难点：

1、RecyclerView的滑动问题

2、RecyclerView的点击问题

3、绘制SideBar

**先来看个图，看是不是你想要的**

![1557800237747.gif](https://upload-images.jianshu.io/upload_images/11096522-4ebcb3ec33394e5d.gif?imageMogr2/auto-orient/strip)


## 实现思路

根据城市和拼音列表，可以想到多布局，这里无非是把城市名称按其首字母进行排列后再填充列表，如果给你一组数据{A、城市1、城市2、B、城市3、城市4...}这样的数据让你填充你总会吧，无非就是两种布局，将拼音和汉字的背景设置不同就行；右侧是个自定义布局，别说你不会自定义布局，不会也行，这个很简单，无非是平分高度，通过`drawText()`绘制字母，然后进行滑动监听，右侧滑动或点击到哪里，左侧列表相应进行滚动即可。

其实原先我已经通过ListView做过了，这次回顾使用RecyclerView再实现一次，发现还遇到了一些新东西，带你们看看。这次没有使用BaseQuickAdapter，使用多了都忘记原始的代码怎么敲了话不多说开撸吧

## 1\. 确定数据格式

首先我们需要确定下Bean的数据格式，毕竟涉及到多布局
```java
public class ItemBean {

    private String itemName;//城市名或者字母A...
    private String itemType;//类型，区分是首字母还是城市名，是首字母的写“head”,不是的填入其它字母都行

    // 标记  拼音头，head为0
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
	......Get Set方法  
}
```

可以看到有两个字段，一个用来显示城市名或者字母，另一个用来区分是城市还是首字母。这里定义了个getType()方法，为字母的话返回0，城市名返回1

## 2\. 整理数据

一般我们准备的数据都是这样的
```java
<resources>
    <string-array name ="mycityarray">
        <item>北京市</item>
        <item>上海市</item>
        <item>广州市</item>
        <item>天津市</item>
        <item>石家庄市</item>
        <item>唐山市</item>
        <item>秦皇岛市</item>
        <item>邯郸市</item>
        <item>邢台市</item>
        <item>保定市</item>
        <item>张家口市</item>
        <item>承德市市</item>
        <item>沧州市</item>
        <item>廊坊市</item>
        <item>衡水市</item>
        ......
	</string-array>
</resources>
```

想要得到我们那样的数据，需要先获取这些城市名的首字母然后进行排序，这里我使用pinyin4j-2.5.0.jar进行汉字到拼音的转化，[jar下载地址](https://sourceforge.net/projects/pinyin4j/files/)

**2.1 编写工具类**

```java
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
```
**2.2 整理数据**

```java
private List<String> cityList;      //给定的所有的城市名
private List<ItemBean> itemList;    //整理后的所有的item子项，可能是城市、可能是字母

//初始化数据，将所有城市进行排序，且加上字母和它们一起形成新的集合
private void initData(){
    
    itemList = new ArrayList<>();
    //获取所有的城市名
    String[] cityArray = getResources().getStringArray(R.array.mycityarray);
    cityList = Arrays.asList(cityArray);
    //将所有城市进行排序，排完后cityList内所有的城市名都是按首字母进行排序的
    Collections.sort(cityList, new CityComparator());           
	
    //将剩余的城市加进去
    for (int i = 0; i < cityList.size(); i++) {

        String city = cityList.get(i);
        String letter = null;                          //当前所属的字母
        
        if (city.contains("重庆")) {
            letter = HanziToPinYin.toPinYin("崇庆") + "";
        } else {
            letter = HanziToPinYin.toPinYin(cityList.get(i)) + "";
        }

        if (letter.equals(currentLetter)) {           //在A字母下，属于当前字母
            itemBean = new ItemBean();
            itemBean.setItemName(city);             //把汉字放进去
            itemBean.setItemType(letter);           //这里放入其它不是“head”的字符串就行
            itemList.add(itemBean);
        } else {                                 //不在当前字母下，先将该字母取出作为独立的一个item
            //添加标签(B...)
            itemBean = new ItemBean();
            itemBean.setItemName(letter);           //把首字母进去
            itemBean.setItemType("head");          //把head标签放进去
            currentLetter = letter;
            itemList.add(itemBean);

            //添加城市
            itemBean = new ItemBean();
            itemBean.setItemName(city);             //把汉字放进去
            itemBean.setItemType(letter);           //把拼音放进去
            itemList.add(itemBean);
        }
    }           
}
```
经过以上步骤就将原先的数据整理成了以下形式排列的一组数据
```java
{
    {itemName:"A",itemType:"head"}
    {itemName:"阿拉善盟",itemType:"A"}
    {itemName:"安抚市",itemType:"A"}
    ...
    {itemName:"巴中市",itemType:"B"}  
    {itemName:"白山市",itemType:"B"}
    ....
}
```

等等，上面有个`Collections.sort(cityList, new CityComparator());`和`letter = HanziToPinYin.toPinYin("崇庆") + "";`你可能还会有疑惑，我就来多几嘴
因为pinyin4j.jar这个jar包在将汉字转为拼音的时候，会将重庆的拼音转为zhongqin，所以在排序和获取首字母的时候都需要单独处理

```java
public class CityComparator implements Comparator<String> {

    private RuleBasedCollator collator;

    public CityComparator() {
        collator = (RuleBasedCollator) Collator.getInstance(Locale.CHINA);
    }

    @Override
    public int compare(String lhs, String rhs) {

        lhs = lhs.replace("重庆", "崇庆");
        rhs = rhs.replace("重庆", "崇庆");
        CollationKey c1 = collator.getCollationKey(lhs);
        CollationKey c2 = collator.getCollationKey(rhs);

        return c1.compareTo(c2);
    }
}
```

这里先指定`RuleBasedCollator`语言环境为CHINA,然后在`compare()`比较方法里，如果遇到两边有"重庆"的字符串，就将其替换为”崇庆“,然后通过`getCollationKey()`获取首个字符然后进行比较。

`letter = HanziToPinYin.toPinYin("崇庆") + "";`获取首字母的时候也是同样，不是获取"重庆"的首字母而是"崇庆"的首字母。

看到这样的一组数据你总会根据多布局来给RecyclerView填充数据了吧

## 3\. RecyclerView填充数据

既然涉及到多布局，那么有几种布局就该有几个`ViewHolder`，这次我将采用原始的写法，不用BaseQuickAdapter，那个太方便搞得我原始的都不会写了

新建CityAdapter类，让这个适配器继承自RecyclerView.Adapter，并将泛型指定为RecyclerView.ViewHolder，其代表我们在CityAdapter中定义的内部类

```java
public class CityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    
    ......
    //字母头
    public static class HeadViewHolder extends RecyclerView.ViewHolder {
        private TextView tvHead;
        public HeadViewHolder(View itemView) {
            super(itemView);
            tvHead = itemView.findViewById(R.id.tv_item_head);
        }
    }

    //城市
    public static class CityViewHolder extends RecyclerView.ViewHolder {

        private TextView tvCity;
        public CityViewHolder(View itemView) {
            super(itemView);
            tvCity = itemView.findViewById(R.id.tv_item_city);
        }
    }
}
```
重写`onCreateViewHolder()`、`onBindViewHolder()`、`getItemCount()`方法，因为涉及多布局，还需重写`getItemViewType()`方法来区分是哪种布局

**完整代码如下**

```java
public class CityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //数据项
    private List<ItemBean> dataList;
    //点击事件监听接口
    private OnRecyclerViewClickListener onRecyclerViewClickListener;

    public void setOnItemClickListener(OnRecyclerViewClickListener onItemClickListener) {
        this.onRecyclerViewClickListener = onItemClickListener;
    }
    public CityAdapter(List<ItemBean> dataList) {
        this.dataList = dataList;
    }
    //创建ViewHolder实例
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        
        if (viewType == 0) {    //Head头字母名称
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_head, viewGroup,false);
            RecyclerView.ViewHolder headViewHolder = new HeadViewHolder(view);
            return headViewHolder;
        } else {             //城市名
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_city, viewGroup,false);
            RecyclerView.ViewHolder cityViewHolder = new CityViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onRecyclerViewClickListener != null) {
                        onRecyclerViewClickListener.onItemClickListener(v);
                    }
                }
            });
            return cityViewHolder;
        }
    }
    //对子项数据进行赋值
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        int itemType = dataList.get(position).getType();
        if (itemType == 0) {
            HeadViewHolder headViewHolder = (HeadViewHolder) viewHolder;
            headViewHolder.tvHead.setText(dataList.get(position).getItemName());
        } else {
            CityViewHolder cityViewHolder = (CityViewHolder) viewHolder;
            cityViewHolder.tvCity.setText(dataList.get(position).getItemName());
        }
    }
    //数据项个数
    @Override
    public int getItemCount() {
        return dataList.size();
    }
    //区分布局类型
    @Override
    public int getItemViewType(int position) {
        int type = dataList.get(position).getType();
        return type;
    }
    //字母头
    public static class HeadViewHolder extends RecyclerView.ViewHolder {
        private TextView tvHead;
        public HeadViewHolder(View itemView) {
            super(itemView);
            tvHead = itemView.findViewById(R.id.tv_item_head);
        }
    }
    //城市
    public static class CityViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCity;
        public CityViewHolder(View itemView) {
            super(itemView);
            tvCity = itemView.findViewById(R.id.tv_item_city);
        }
    }
}
```

两种item布局都是只放了一个TextView控件

这里有两处自己碰到和当时使用ListView不同的地方：

1、`RecyclerView`没有`setOnItemClickListener()`，需要自己定义接口来实现
2、自己平时加载布局都直接是`View view = LayoutInflater.from(context).inflate(R.layout.item_head, null);`，也没发现什么问题，但此次就出现了Item子布局无法横向铺满父布局。 解决办法：将改为以下方式加载布局

```java
View view = LayoutInflater.from(context).inflate(R.layout.item_head, viewGroup,false);
```

（如果遇到不能铺满状况也可能是RecyclerView没有明确宽高而是用权重代替的原因）

建立的监听器

```java
public interface OnRecyclerViewClickListener {
    void onItemClickListener(View view);
}

```

## 4\. 绘制侧边字母栏

这里的自定义很简单，无非是定义画笔，然后在画布上通过drawText()方法来绘制Text即可。

**4.1 首先定义类SideBar继承自View，重写构造方法，并在三个方法内调用自定义的init();方法来初始化画笔**

```java
public class SideBar extends View {
    //画笔
    private Paint paint;
    
    public SideBar(Context context) {
        super(context);
        init();
    }
    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public SideBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    //初始化画笔工具
    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);//抗锯齿
    }   
}
```

**4.2 在onDraw()方法里绘制字母**

```java
public static String[] characters = new String[]{"❤", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
private int position = -1;		//当前选中的位置
private int defaultTextColor = Color.parseColor("#D2D2D2");   //默认拼音文字的颜色 
private int selectedTextColor = Color.parseColor("#2DB7E1");  //选中后的拼音文字的颜色 
   
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    int height = getHeight();						//当前控件高度
    int width = getWidth();						 	//当前控件宽度
    int singleHeight = height / characters.length;    //每个字母占的长度

    for (int i = 0; i < characters.length; i++) {
        if (i == position) {                    //当前选中
            paint.setColor(selectedTextColor); 	//设置选中时的画笔颜色
        } else {                                //未选中
            paint.setColor(defaultTextColor);	//设置未选中时的画笔颜色
        }
        paint.setTextSize(textSize);			//设置字体大小

        //设置绘制的位置
        float xPos = width / 2 - paint.measureText(characters[i]) / 2;
        float yPos = singleHeight * i + singleHeight;
        
        canvas.drawText(characters[i], xPos, yPos, paint);      //绘制文本
    }
}
```
通过以上两步，右侧边栏就算绘制完成了，但这只是静态的，如果要实现侧边栏滑动的时候，我们还需要监听其触摸事件

**4.3 定义触摸回调接口和设置监听器的方法**

```java
//设置触摸位置改变的监听器的方法
public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
    this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
}

//触摸位置更改的接口
public interface OnTouchingLetterChangedListener {
    void onTouchingLetterChanged(int position);
}
```

**4.4 触摸事件**

```java
@Override
public boolean onTouchEvent(MotionEvent event) {

    int action = event.getAction();
    float y = event.getY();
    position = (int) (y / (getHeight() / characters.length));	//获取触摸的位置

    if (position >= 0 && position < characters.length) {        
        //触摸位置变化的回调
        onTouchingLetterChangedListener.onTouchingLetterChanged(position);
        
        switch (action) {
            case MotionEvent.ACTION_UP:
                setBackgroundColor(Color.TRANSPARENT);//手指起来后的背景变化
                position = -1;
                invalidate();//重新绘制控件
                if (text_dialog != null) {
                    text_dialog.setVisibility(View.INVISIBLE);
                }
                break;
            default://手指按下
                setBackgroundColor(touchedBgColor);
                invalidate();
                text_dialog.setText(characters[position]);//字母框的弹出
                break;
        }
    } else {
        setBackgroundColor(Color.TRANSPARENT);
        if (text_dialog != null) {
            text_dialog.setVisibility(View.INVISIBLE);
        }
    }
    return true;	//一定要返回true，表示拦截了触摸事件
}
```
具体的解释如代码所示，当手指起来时，position为-1，当手指按下，更改背景并弹出字母框(这里的字母框其实就是一个TextView,通过显示隐藏来表示其弹出)

## 5\. Activity中使用

`itemList`数据填充那些就不写了,在前面整理数据那部分

```java
//所有的item子项，可能是城市、可能是字母
private List<ItemBean> itemList;    
//目标项是否在最后一个可见项之后
private boolean mShouldScroll;
//记录目标项位置(要移动到的位置)
private int mToPosition;

@Override
protected void onCreate(Bundle savedInstanceState) {
    //为左侧RecyclerView设立Item的点击事件
    cityAdapter.setOnItemClickListener(this);

     sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(int position) {
                
                String city_label = SideBar.characters[position];      //滑动到的字母
                for (int i = 0; i < cityList.size(); i++) {
                    if (itemList.get(i).getItemName().equals(city_label)) {
                        moveToPosition(i);                         //直接滚过去
//                        smoothMoveToPosition(recyclerView,i);       //平滑的滚动
                        tvDialog.setVisibility(View.VISIBLE);
                        break;
                    }
                    if (i == cityList.size() - 1) {
                        tvDialog.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });    
}

//实战中可能会有选择完后此页面关闭，返回当前数据等操作，可在此处完成
@Override
public void onItemClickListener(View view) {
    int position = recyclerView.getChildAdapterPosition(view);
    Toast.makeText(view.getContext(), itemList.get(position).getItemName(), Toast.LENGTH_SHORT).show();
}
```
在使用`ListView`的时候，知道要移动到的位置`position`时，直接`listView.setSelection(position)`就可将当前的item移动到屏幕顶部，而RecyclerView的scrollToPosition(position)只是将item移动到屏幕内，所以需要我们通过`scrollToPositionWithOffset()`方法将其置顶

```java
private void moveToPosition(int position) {
    if (position != -1) {
        recyclerView.scrollToPosition(position);
        LinearLayoutManager mLayoutManager =
                (LinearLayoutManager) recyclerView.getLayoutManager();
        mLayoutManager.scrollToPositionWithOffset(position, 0);
    }
}
```

这里还有一种平滑的滚动方式，具体见[Demo](https://github.com/myfittinglife/SlideBarCitySelectDemo)

## 6\. 总结

再次说明下自己遇到的几个问题：

1、点击问题，`ListView`有`setOnItemClickListener()`方法，而`RecyclerView`没有，需要建立接口进行监听。
2、滑动问题，`listView`的`setSelection(position)`滑动可以直接将该项滑至屏幕顶部，而`recyclerView`的 `smoothScrollToPosition(position);`只是将其移动至屏幕内，需要再次进行处理。
3、`listView`的`isEnable()` 方法可以设置字母Item不能点击，而城市名Item可以点击，`recycleView`的实现(直接在设立点击事件的时候，是头部就不设立点击事件就行)
4、`item`不充满全屏，加载布局的原因

以上就是全部内容，真的是不写文章不回顾就会忘得很快啊，以前还写过仿美团的双RecyclerView联动，当时关于如何滑动就写了很多，到这里就忘了该怎么将item置顶，真是汗颜，下次抽时间把那篇文章也总结下吧。

如果对你有帮助的话记得start哦

## 7\. 待改善

最关键的还是数据的处理那里

1、整理数据的部分，每次添加数据都判断下是否包含重庆感觉挺傻的，可以将全部数据填充完后，在指定位置加上重庆就行，需要优化
2、在`sideBar`的`setOnTouchingLetterChangedListener()`方法里，每次滑动完都从`cityList`里0开始找第一个出现该字母的位置，感觉很傻，需要优化
3、为了方便的展示，没有进行封装，其实还可以将一些例如设置侧边栏字体颜色背景等都封装起来，便于更改，但鉴于有些小伙伴不会自定义`View`（我懒），所以就没有写了，下次再整理整理吧。

各位小伙伴觉得哪些地方还可以优化呢？

## 参考文章

[Android项目实战（八）：列表右侧边栏拼音展示效果](https://www.cnblogs.com/xqxacm/p/4951691.html)
[RecyclerView将指定项滑动到顶部显示](https://blog.csdn.net/zhangqunshuai/article/details/81506790)
[java.text 类 CollationKey](http://blog.sina.com.cn/s/blog_46debefc0100jfvd.html)
[RecycleView4种定位滚动方式演示](https://www.jianshu.com/p/3acc395ae933)



