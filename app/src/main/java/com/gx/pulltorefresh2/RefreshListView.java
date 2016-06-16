package com.gx.pulltorefresh2;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


/**
 * Created by Administrator on 2016/3/26.
 * 自定义ListView
 */
public class RefreshListView extends ListView implements AbsListView.OnScrollListener{
    private static final int DONE=0;
    private static final int PULL_TO_REFRESH=1;
    private static final int RELEASE_TO_REFRESH=2;
    private static final int REFRESHING=4;
    private static final int RATIO=3;
    private LinearLayout headerView;
    private int headerViewHeight;
    private float startY;
    private float offsetY;

    private TextView tv_pull_to_refresh;
    private OnRefreshListener monRefreshListener;
    private int state;
    private int mFirstVisibleItem;
    private boolean isRecord;
    private boolean isEnd;
    private boolean isRefreable;

    //帧动画效果
    private RefreshThirdStepView mThirdView;
    private AnimationDrawable thirdAnim;

    public RefreshListView(Context context) {
        super(context);
        init(context);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 布局及其视图等初始化
     * @param context
     */
    private void init(Context context){
        //设置边界无回弹效果
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        //设置滑动监听器
        setOnScrollListener(this);
       //头布局初始化
        headerView=(LinearLayout) LayoutInflater.from(context).inflate(R.layout.head_item, null, false);
        tv_pull_to_refresh=(TextView)headerView.findViewById(R.id.tv_pull_to_refresh);
        //帧动画初始化
        mThirdView=(RefreshThirdStepView)headerView.findViewById(R.id.third_view);
        mThirdView.setBackgroundResource(R.drawable.pull_to_refresh_third_anim);
        thirdAnim=(AnimationDrawable) mThirdView.getBackground();

        measureView(headerView);
        addHeaderView(headerView);

        //得到头布局的高度
        headerViewHeight=headerView.getMeasuredHeight();
        headerView.setPadding(0,-headerViewHeight,0,0);
        Log.d("ss", -headerViewHeight + "-headerViewHeight");

        //刷新状态已经完成
        state=DONE;
        //是否结束
        isEnd=true;
        //是否可刷新
        isRefreable=false;


    }

    /**
     * 滚动状态发生改变
     * @param view
     * @param scrollState
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    /**
     * 滚动
     * @param view
     * @param firstVisibleItem 当前看到的第一个表单项ID
     * @param visibleItemCount 当前能看见列表项个数（小半个也算）
     * @param totalItemCount 列表项共数
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
               mFirstVisibleItem=firstVisibleItem;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //如果现在时结束的状态，即刷新完毕了，可以再次刷新了，在onRefreshComplete中设置
        if(isEnd){
            if(isRefreable){//如果现在是可刷新状态   在setOnRefreshListener中设置为true
                switch (ev.getAction()){
                    //用户按下
                    case MotionEvent.ACTION_DOWN:
                        //如果当前在listView顶部并且没有记录y坐标
                        if(mFirstVisibleItem==0&&!isRecord){
                            //将isRecord置为true，说明现在已记录y坐标
                            isRecord = true;
                            //将当前y坐标赋值给startY起始y坐标
                            startY = ev.getY();
                        }
                        break;
                    //用户滑动
                    case MotionEvent.ACTION_MOVE:
                        //再次得到y坐标，用来和startY相减来计算offsetY位移值
                        float tempY = ev.getY();
                        //再起判断一下是否为listview顶部并且没有记录y坐标
                        if (mFirstVisibleItem == 0 && !isRecord) {
                            isRecord = true;
                            startY = tempY;
                        }
                        //如果当前状态不是正在刷新的状态，并且已经记录了y坐标
                        if (state!=REFRESHING && isRecord ) {
                            //计算y的偏移量
                            offsetY = tempY - startY;

                            //如果当前的状态是放开刷新，并且已经记录y坐标
                            if (state == RELEASE_TO_REFRESH && isRecord) {
                                setSelection(0);
                                //如果当前滑动的距离小于headerView的总高度
                                if (-headerViewHeight+offsetY/RATIO<0) {
                                    //将状态置为下拉刷新状态
                                    state = PULL_TO_REFRESH;
                                    //根据状态改变headerView，主要是更新动画和文字等信息
                                    changeHeaderByState(state);
                                    //如果当前y的位移值小于0，即为headerView隐藏了
                                }else if (offsetY<=0) {
                                    //将状态变为done
                                    state = DONE;
                                    //根据状态改变headerView，主要是更新动画和文字等信息
                                    changeHeaderByState(state);
                                }
                            }
                            //如果当前状态为下拉刷新并且已经记录y坐标
                            if (state == PULL_TO_REFRESH && isRecord) {
                                setSelection(0);
                                //如果下拉距离大于等于headerView的总高度
                                if (-headerViewHeight+offsetY/RATIO>=0) {
                                    //将状态变为放开刷新
                                    state = RELEASE_TO_REFRESH;
                                    //根据状态改变headerView，主要是更新动画和文字等信息
                                    changeHeaderByState(state);
                                    //如果当前y的位移值小于0，即为headerView隐藏了
                                }else if (offsetY<=0) {
                                    //将状态变为done
                                    state = DONE;
                                    //根据状态改变headerView，主要是更新动画和文字等信息
                                    changeHeaderByState(state);
                                }
                            }
                            //如果当前状态为done并且已经记录y坐标
                            if (state == DONE && isRecord) {
                                //如果位移值大于0
                                if (offsetY>=0) {
                                    //将状态改为下拉刷新状态
                                    state = PULL_TO_REFRESH;
                                }
                            }
                            //如果为下拉刷新状态
                            if (state == PULL_TO_REFRESH) {
                                //则改变headerView的padding来实现下拉的效果
                                headerView.setPadding(0,(int)(-headerViewHeight+offsetY/RATIO) ,0,0);

                            }
                            //如果为放开刷新状态
                            if (state == RELEASE_TO_REFRESH) {
                                //改变headerView的padding值
                                headerView.setPadding(0,(int)(-headerViewHeight+offsetY/RATIO) ,0, 0);

                            }
                        }
                        break;
                    //当用户手指抬起时
                    case MotionEvent.ACTION_UP:
                        //如果当前状态为下拉刷新状态
                        if (state == PULL_TO_REFRESH) {
                            //平滑的隐藏headerView
                            this.smoothScrollBy((int)(-headerViewHeight+offsetY/RATIO)+headerViewHeight, 500);
                            //根据状态改变headerView
                            changeHeaderByState(state);
                        }
                        //如果当前状态为放开刷新
                        if (state == RELEASE_TO_REFRESH) {
                            //平滑的滑到正好显示headerView
                            this.smoothScrollBy((int)(-headerViewHeight+offsetY/RATIO), 500);
                            //将当前状态设置为正在刷新
                            state = REFRESHING;
                            //回调接口的onRefresh方法
                            monRefreshListener.onRefresh();
                            //根据状态改变headerView
                            changeHeaderByState(state);
                        }
                        //这一套手势执行完，一定别忘了将记录y坐标的isRecord改为false，以便于下一次手势的执行
                        isRecord = false;
                        break;
                }

            }
        }
        return super.onTouchEvent(ev);
    }

    public interface OnRefreshListener{
        void onRefresh();
    }
    /**
     * 回调接口，想实现下拉刷新的listview实现此接口
     * @param onRefreshListener
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener){
        monRefreshListener = onRefreshListener;
        isRefreable = true;
    }

    /**
     * 刷新完毕，从主线程发送过来，并且改变headerView的状态和文字动画信息
     */
    public void setOnRefreshComplete(){
        //一定要将isEnd设置为true，以便于下次的下拉刷新
        isEnd = true;
        state = DONE;

        changeHeaderByState(state);
    }
    /**
     * 根据状态改变headerView的动画和文字显示
     * @param state
     */
    private void changeHeaderByState(int state){
        switch (state) {
            case DONE://如果的隐藏的状态
                //设置headerView的padding为隐藏
                headerView.setPadding(0, -headerViewHeight, 0, 0);


                //停止第三状态的动画
                thirdAnim.stop();
                break;
            case RELEASE_TO_REFRESH://当前状态为放开刷新
                //文字显示为放开刷新
                tv_pull_to_refresh.setText("放开刷新...");


                //停止第三状态的动画
                thirdAnim.stop();
                break;
            case PULL_TO_REFRESH://当前状态为下拉刷新
                //设置文字为下拉刷新
                tv_pull_to_refresh.setText("下拉刷新...");

                //第三状态动画停止
                thirdAnim.stop();
                break;
            case REFRESHING://当前状态为正在刷新
                //文字设置为正在刷新
                tv_pull_to_refresh.setText("正在刷新...");

                //启动第三状态view
                thirdAnim.start();
                break;
            default:
                break;
        }
    }

    /**
     *给布局设置大小
     * @param child
     */

    private void measureView(View child){
        ViewGroup.LayoutParams p=child.getLayoutParams();
        if(p==null){
            p=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        }
        int childWidthSpec=ViewGroup.getChildMeasureSpec(0,0+0,p.width);
        int lpHeight=p.height;
        int childHeightSpec;
        if(lpHeight>0){
            childHeightSpec=MeasureSpec.makeMeasureSpec(lpHeight,MeasureSpec.EXACTLY);

        }else {
            childHeightSpec=MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);

        }
        child.measure(childWidthSpec,childHeightSpec);
    }
}
