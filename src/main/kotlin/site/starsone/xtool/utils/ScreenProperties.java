package site.starsone.xtool.utils;

import java.awt.*;
import java.util.Arrays;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * @author StarsOne
 * @url <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2022/01/03 16:07
 */

public class ScreenProperties {

    /**
     * 最大化时的屏幕高度（不会覆盖任务栏）
     */
    private double maximizeHeight;
    /**
     * 最大化时的屏幕宽度（不会覆盖任务栏）
     */
    private double maximizeWidth;
    /**
     * 全屏时屏幕高度（覆盖任务栏）
     */
    private double fullScreenHeight;
    /**
     * 全屏时屏幕宽度（覆盖任务栏）
     */
    private double fullScreenWidth;
    /**
     * 全屏时屏幕的左上角x坐标（覆盖任务栏）
     */
    private double fullScreenX;
    /**
     * 全屏时屏幕的左上角y坐标（覆盖任务栏）
     */
    private double fullScreenY;

    /**
     * 最大化时左上角的x轴坐标（不会覆盖任务栏，因为任务栏可能在“上下左右”四个位置，所以这个值会变动）
     */
    private double minX;
    /**
     * 最大化时左上角的y轴坐标（不会覆盖任务栏，因为任务栏可能在“上下左右”四个位置，所以这个值会变动）
     */
    private double minY;
    /**
     * 最大化时右下角的x轴坐标（不会覆盖任务栏，因为任务栏可能在“上下左右”四个位置，所以这个值会变动）
     */
    private double maxX;
    /**
     * 最大化时右下角的y轴坐标（不会覆盖任务栏，因为任务栏可能在“上下左右”四个位置，所以这个值会变动）
     */
    private double maxY;
    /**
     * 最大化时屏幕中心点的x坐标
     */
    private double centerX;
    /**
     * 最大化时屏幕中心点的y坐标
     */
    private double centerY;

    /**
     * 主界面为正常大小窗口时，默认的左上角x坐标
     */
    private double indexX;
    /**
     * 主界面为正常大小窗口时，默认的左上角y坐标
     */
    private double indexY;
    /**
     * 主界面为正常大小窗口时的窗口的宽度
     */
    private double indexWidth;
    /**
     * 主界面为正常大小窗口时的窗口的高度
     */
    private double indexHeight;

    /**
     * 默认当前窗口在第几块屏幕，就在第几块屏幕显示
     */
    public static final int DEFAULT = 0;
    /**
     * 在第一块屏幕显示
     */
    public static final int FIRST_SCREEN = 1;
    /**
     * 在第二块屏幕显示
     */
    public static final int SECOND_SCREEN = 2;

    /**
     * 第一屏幕,全屏窗口大小（覆盖任务栏）
     */
    private Rectangle firstRectangle;
    /**
     * 第一屏幕,最大化窗口大小（不覆盖任务栏）
     */
    private Rectangle2D firstRectangle2D;
    /**
     * 第二屏幕,全屏窗口大小（覆盖任务栏）
     */
    private Rectangle secondRectangle;
    /**
     * 第二屏幕,最大化窗口大小（不覆盖任务栏）
     */
    private Rectangle2D secondRectangle2D;

    /**
     * 获得所有屏幕设备的信息
     */
    private GraphicsDevice[] gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

    /**
     * 构造函数
     *
     * @param stage 当前stage对象
     * @param index {@link ScreenProperties#DEFAULT} 默认
     *              {@link ScreenProperties#FIRST_SCREEN} 在第一块屏幕显示
     *              {@link ScreenProperties#SECOND_SCREEN} 在第二块屏幕显示
     */
    public ScreenProperties(Stage stage, int index) {
        if (stage == null) {
            return;
        }
        // 当前stage的中心点的x坐标(当屏幕位置为"第二屏幕-第一屏幕"的时候，该值小于0)
        double centerXOfStage = stage.getX() + stage.getWidth() / 2;
        // 初始化firstRectangle和secondRectangle
        initRectangle();
        // 只有一个屏幕的情况下，不管index参数是什么，都是在第一屏幕显示
        if (gd.length == 1) {
            initFirstDevice();
        } else {
            // 多个屏幕的情况(这里只适配2个屏幕的情况)
            if (index == FIRST_SCREEN) {
                // 在第一块屏幕显示
                initFirstDevice();
            } else if (index == SECOND_SCREEN) {
                // 在第二块屏幕显示
                initSecondDevice();
            } else {
                // 其它情况, 当前stage在第几块屏幕，就在第几块屏幕显示（根据Stage的中心点来判断它在第几块屏幕）
                // 如果第二块屏幕的左上角坐标小于0，说明当前屏幕位置为：第二屏幕 - 第一屏幕（第二块屏幕在第一块屏幕的左边）
                if (secondRectangle2D.getMinX() < 0) {
                    // 此时第二屏幕的x坐标都是小于0的
                    if (centerXOfStage < 0) {
                        // 窗口的中心点在第二块屏幕，则在第二屏幕显示
                        initSecondDevice();
                    } else {
                        // 窗口的中心点在第一个屏幕，则在第一屏幕显示
                        initFirstDevice();
                    }
                } else {
                    // 当前屏幕位置：第一屏幕 - 第二屏幕（第二块屏幕在第一块屏幕的右边），此时第二屏幕x坐标是大于第一屏幕的宽度
                    if (centerXOfStage > firstRectangle.width) {
                        // 窗口的中心点在第二块屏幕，则在第二屏幕显示
                        initSecondDevice();
                    } else {
                        // 窗口的中心点在第一个屏幕，则在第一屏幕显示
                        initFirstDevice();
                    }
                }
            }
        }
    }

    /**
     * 初始化第一个屏幕的信息
     */
    private void initFirstDevice() {
        initDeviceInfo(firstRectangle, firstRectangle2D);
        // 全屏时的x坐标为最大化时左上角的x坐标
        fullScreenX = 0;
        // 全屏时的y坐标为0
        fullScreenY = 0;
    }

    /**
     * 初始化第二块屏幕的信息
     */
    private void initSecondDevice() {
        initDeviceInfo(secondRectangle, secondRectangle2D);
        // 如果屏幕位置："第二屏幕-第一屏幕"
        if (secondRectangle2D.getMinX() < 0) {
            // 此时全屏的x坐标为0 减去自己（第二块）的屏幕宽度
            fullScreenX = -secondRectangle.width;
        } else {
            // 此时全屏的x坐标为 第一块屏幕的宽度
            fullScreenX = firstRectangle.width;
        }
        // 全屏时的y坐标为0
        fullScreenY = 0;
    }

    /**
     * 初始化屏幕信息
     *
     * @param rectangle   全屏
     * @param rectangle2D 最大化
     */
    private void initDeviceInfo(Rectangle rectangle, Rectangle2D rectangle2D) {
        // 全屏时
        fullScreenWidth = rectangle.width;
        fullScreenHeight = rectangle.height;
        // 最大化
        maximizeWidth = rectangle2D.getWidth();
        maximizeHeight = rectangle2D.getHeight();
        // 最大化时右上角坐标
        minX = rectangle2D.getMinX();
        minY = rectangle2D.getMinY();
        // 最大化时左下角坐标
        maxX = rectangle2D.getMaxX();
        maxY = rectangle2D.getMaxY();
        // 最大化时的中心点位置(最大化时左上角坐标 + 最大化时窗口宽度的一半)
        centerX = minX + maximizeWidth / 2;
        centerY = minY + maximizeHeight / 2;
        // 主页面处于正常窗口大小时的宽度(为最大化时的宽度的八分之一)
        indexWidth = maximizeWidth * 0.9;
        // 主页面处于正常窗口大小时的高度(为最大化时的宽度的九分之一)
        indexHeight = maximizeHeight * 0.9;
        // 主页面为正常窗口大小时的右上角x坐标(为最大化时左上角的x坐标 + 最大化时的宽度的五分之一)
        indexX = minX + maximizeWidth * 0.05;
        // 主页面为正常窗口大小时的右上角y坐标(为最大化时左上角的y坐标 + 为最大化时的高度的五分之一)
        indexY = minY + maximizeHeight * 0.05;

    }

    /**
     * 非常奇葩的一点：通过GraphicsDevice[] gd = ge.getScreenDevices();取得的设备是按照从左往右的顺序得到的
     * 例如现在屏幕位置：第二屏幕 - 第一屏幕，则gd[0]是第二屏幕，gd[1]是第一屏幕
     * 例如现在屏幕位置：第一屏幕 - 第二屏幕，则gd[0]是第一屏幕，gd[1]是第二屏幕
     * 太坑了吧！！！
     * 更坑爹的是，使用Screen secondScreen = Screen.getScreens().get(0); 其中get(0)为第一屏幕,get(1)为第二屏幕（不会因为屏幕位置排序而变化）
     * 如果你有更好的获取两个屏幕下设备的信息的方法，你可以更改下
     */
    private void initRectangle() {
        // 获得第一个屏幕最大化窗口的信息（不覆盖任务栏）
        Screen firstScreen = Screen.getScreens().get(0);
        firstRectangle2D = firstScreen.getVisualBounds();
        // 只有一块屏幕
        if (gd.length == 1) {
            // 获得第一个屏幕全屏时的信息(覆盖任务栏)
            firstRectangle = gd[0].getDefaultConfiguration().getBounds();
        } else {
            // 多块屏幕（只适配2块）
            // 获得第二个屏幕最大化窗口的信息（不覆盖任务栏）
            Screen secondScreen = Screen.getScreens().get(1);
            secondRectangle2D = secondScreen.getVisualBounds();
            // 左边的屏幕（全屏）
            Rectangle leftRectangle = gd[0].getDefaultConfiguration().getBounds();
            // 右边的屏幕（全屏）
            Rectangle rightRectangle = gd[1].getDefaultConfiguration().getBounds();
            // 屏幕位置：第二屏幕 - 第一屏幕
            if (secondRectangle2D.getMinX() < 0) {
                secondRectangle = leftRectangle;
                firstRectangle = rightRectangle;
            } else {
                // 屏幕位置：第一屏幕 - 第二屏幕
                firstRectangle = leftRectangle;
                secondRectangle = rightRectangle;
            }
        }
    }

    public double getMaximizeHeight() {
        return maximizeHeight;
    }

    public double getMaximizeWidth() {
        return maximizeWidth;
    }

    public double getFullScreenHeight() {
        return fullScreenHeight;
    }

    public double getFullScreenWidth() {
        return fullScreenWidth;
    }

    public double getFullScreenX() {
        return fullScreenX;
    }

    public double getFullScreenY() {
        return fullScreenY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public double getIndexX() {
        return indexX;
    }

    public double getIndexY() {
        return indexY;
    }

    public double getIndexWidth() {
        return indexWidth;
    }

    public double getIndexHeight() {
        return indexHeight;
    }

    @Override
    public String toString() {
        return "ScreenProperties{" +
                "maximizeHeight=" + maximizeHeight +
                ", maximizeWidth=" + maximizeWidth +
                ", fullScreenHeight=" + fullScreenHeight +
                ", fullScreenWidth=" + fullScreenWidth +
                ", fullScreenX=" + fullScreenX +
                ", fullScreenY=" + fullScreenY +
                ", minX=" + minX +
                ", minY=" + minY +
                ", maxX=" + maxX +
                ", maxY=" + maxY +
                ", centerX=" + centerX +
                ", centerY=" + centerY +
                ", indexX=" + indexX +
                ", indexY=" + indexY +
                ", indexWidth=" + indexWidth +
                ", indexHeight=" + indexHeight +
                ", firstRectangle=" + firstRectangle +
                ", firstRectangle2D=" + firstRectangle2D +
                ", secondRectangle=" + secondRectangle +
                ", secondRectangle2D=" + secondRectangle2D +
                ", gd=" + Arrays.toString(gd) +
                '}';
    }
}

