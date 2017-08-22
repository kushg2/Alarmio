/*
 * Copyright 2015 Alexandre Piveteau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package james.alarmio.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.afollestad.aesthetic.Aesthetic;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import james.alarmio.utils.ConversionUtils;

public class PageIndicatorView extends BaseSubscriptionView implements ViewPager.OnPageChangeListener {

    private int actualPosition;
    private float offset;
    private int size;
    private ViewPager viewPager;

    private IndicatorEngine engine;

    private int textColorPrimary;
    private int textColorSecondary;

    private Disposable textColorPrimarySubscription;
    private Disposable textColorSecondarySubscription;

    public PageIndicatorView(Context context) {
        this(context, null);
    }

    public PageIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        engine = new IndicatorEngine();

        engine.onInitEngine(this, context);
        size = 2;

        subscribe();
    }

    @Override
    public void subscribe() {
        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        textColorPrimary = integer;
                        engine.updateTextColors(PageIndicatorView.this);
                        invalidate();
                    }
                });

        textColorSecondarySubscription = Aesthetic.get()
                .textColorSecondary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        textColorSecondary = integer;
                        engine.updateTextColors(PageIndicatorView.this);
                        invalidate();
                    }
                });
    }

    @Override
    public void unsubscribe() {
        textColorPrimarySubscription.dispose();
        textColorSecondarySubscription.dispose();
    }

    public int getTotalPages() {
        return size;
    }

    public int getActualPosition() {
        return actualPosition;
    }

    public float getPositionOffset() {
        return offset;
    }

    public void notifyNumberPagesChanged() {
        size = viewPager.getAdapter().getCount();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        engine.onDrawIndicator(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(engine.getMeasuredWidth(widthMeasureSpec, heightMeasureSpec), engine.getMeasuredHeight(widthMeasureSpec, heightMeasureSpec));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        actualPosition = position;
        offset = positionOffset;
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * You must call this AFTER setting the Adapter for the ViewPager, or it won't display the right amount of points.
     *
     * @param viewPager
     */
    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        viewPager.addOnPageChangeListener(this);
        size = viewPager.getAdapter().getCount();
        invalidate();
    }

    private static class IndicatorEngine {

        private Context context;

        private PageIndicatorView indicator;

        private Paint selectedPaint;
        private Paint unselectedPaint;

        public int getMeasuredHeight(int widthMeasuredSpec, int heightMeasuredSpec) {
            return ConversionUtils.dpToPx(8);
        }

        public int getMeasuredWidth(int widthMeasuredSpec, int heightMeasuredSpec) {
            return ConversionUtils.dpToPx(8 * (indicator.getTotalPages() * 2 - 1));
        }

        public void onInitEngine(PageIndicatorView indicator, Context context) {
            this.indicator = indicator;
            this.context = context;

            selectedPaint = new Paint();
            unselectedPaint = new Paint();

            selectedPaint.setColor(indicator.textColorPrimary);
            unselectedPaint.setColor(indicator.textColorSecondary);
            selectedPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            unselectedPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        }

        public void updateTextColors(PageIndicatorView indicator) {
            selectedPaint.setColor(indicator.textColorPrimary);
            unselectedPaint.setColor(indicator.textColorSecondary);
        }

        public void onDrawIndicator(Canvas canvas) {
            int height = indicator.getHeight();

            for (int i = 0; i < indicator.getTotalPages(); i++) {
                int x = ConversionUtils.dpToPx(4) + ConversionUtils.dpToPx(16 * i);
                canvas.drawCircle(x, height / 2, ConversionUtils.dpToPx(4), unselectedPaint);
            }

            int firstX;
            int secondX;

            firstX = ConversionUtils.dpToPx(4 + indicator.getActualPosition() * 16);

            if (indicator.getPositionOffset() > .5f) {
                firstX += ConversionUtils.dpToPx(16 * (indicator.getPositionOffset() - .5f) * 2);
            }

            secondX = ConversionUtils.dpToPx(4 + indicator.getActualPosition() * 16);

            if (indicator.getPositionOffset() < .5f) {
                secondX += ConversionUtils.dpToPx(16 * indicator.getPositionOffset() * 2);
            } else {
                secondX += ConversionUtils.dpToPx(16);
            }

            canvas.drawCircle(firstX, ConversionUtils.dpToPx(4), ConversionUtils.dpToPx(4), selectedPaint);
            canvas.drawCircle(secondX, ConversionUtils.dpToPx(4), ConversionUtils.dpToPx(4), selectedPaint);
            canvas.drawRect(firstX, 0, secondX, ConversionUtils.dpToPx(8), selectedPaint);
        }
    }
}