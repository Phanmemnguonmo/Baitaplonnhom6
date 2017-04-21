package com.example.lamgame.trytodosth;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;



import java.util.ArrayList;
import java.util.Random;



public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    public static final int WIDTH = 2000;
    public static final int HEIGHT = 1200;
    public static final int MOVESPEED = -15;
    private long smokeStartTime;
    private long missileStartTime;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topborder;
    private ArrayList<BotBorder> botborder;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;

    //Tăng chậm lại sự tiến triển khó khăn, giảm để tăng tốc độ tiến triển khó khăn
    private int progressDenom = 20;

    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best;



    public GamePanel(Context context)
    {
        super(context);


        //thêm gọi lại cho surfaceholder để đánh chặn các sự kiện
        getHolder().addCallback(this);



        //làm GamePanel đặt tiêu điểm để nó có thể xử lý các sự kiện
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        int counter = 0;
        while(retry && counter<1000)
        {
            counter++;
            try{thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;

            }catch(InterruptedException e){e.printStackTrace();}

        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.mp1));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 40, 3);
        smoke = new ArrayList<Smokepuff>();
        missiles = new ArrayList<Missile>();
        topborder = new ArrayList<TopBorder>();
        botborder = new ArrayList<BotBorder>();
        smokeStartTime=  System.nanoTime();
        missileStartTime = System.nanoTime();
        thread = new MainThread(getHolder(), this);
        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();

    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            if(!player.getPlaying() && newGameCreated && reset)
            {
                player.setPlaying(true);
                player.setUp(true);
            }
            if(player.getPlaying())
            {

                if(!started)started = true;
                reset = false;
                player.setUp(true);
            }
            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP)
        {
            player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update()

    {
        if(player.getPlaying()) {

            if(botborder.isEmpty())
            {
                player.setPlaying(false);
                return;
            }
            if(topborder.isEmpty())
            {
                player.setPlaying(false);
                return;
            }

            bg.update();
            player.update();

            // Tính ngưỡng của chiều cao biên giới có thể đã dựa trên điểm số
            // Max và min tim biên giới được cập nhật, và biên giới chuyển hướng khi một trong hai hoặc tối đa
            // Min được đáp ứng

            maxBorderHeight = 30+player.getScore()/progressDenom;
            // Nắp tối đa chiều cao biên giới, để biên giới chỉ có thể đưa ra một tổng của 1/2 màn hình
            if(maxBorderHeight > HEIGHT/4)maxBorderHeight = HEIGHT/4;
            minBorderHeight = 5+player.getScore()/progressDenom;

            // Kiểm tra va chạm biên dưới
            for(int i = 0; i<botborder.size(); i++)
            {
                if(collision(botborder.get(i), player))
                    player.setPlaying(false);
            }

            // Kiểm tra va chạm biên giới hàng đầu
            for(int i = 0; i <topborder.size(); i++)
            {
                if(collision(topborder.get(i),player))
                    player.setPlaying(false);
            }

            // Cập nhật biên giới hàng đầu
            this.updateTopBorder();

            // Cập nhật biên giới phía dưới
            this.updateBottomBorder();

            //add missiles on timer
            long missileElapsed = (System.nanoTime()-missileStartTime)/1000000;
            if(missileElapsed >(2000 - player.getScore()/4)){


                // Tên lửa đầu tiên luôn luôn đi xuống giữa các
//                if(missiles.size()==0)
//                {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.
                        missile),WIDTH + 11, HEIGHT/2, 45, 15, player.getScore(), 13));
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.
                            missile),WIDTH + 12, HEIGHT/3, 45, 15, player.getScore(), 13));
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.
                            missile),WIDTH + 15, HEIGHT/4, 45, 15, player.getScore(), 13));
//                }
//                else
//                {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+9, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+maxBorderHeight),45,15, player.getScore(),13));
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+14, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 4))+maxBorderHeight),45,15, player.getScore(),13));
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+18, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 3))+maxBorderHeight),45,15, player.getScore(),13));
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+11, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 6))+maxBorderHeight),45,15, player.getScore(),13));
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+12, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+maxBorderHeight),45,15, player.getScore(),13));

                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+10, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+maxBorderHeight),45,15, player.getScore(),13));
//                }

                // Thiết lập lại bộ đếm thời gian
                missileStartTime = System.nanoTime();
            }
            // Lặp qua mỗi tên lửa và kiểm tra va chạm và loại bỏ
            for(int i = 0; i<missiles.size();i++)
            {
                // Tên lửa cập nhật
                missiles.get(i).update();

                if(collision(missiles.get(i),player))
                {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                // Loại bỏ tên lửa nếu nó là cách tắt màn hình
                if(missiles.get(i).getX()<-100)
                {
                    missiles.remove(i);
                    break;
                }
            }

            // Thêm nhát khói vào giờ
            long elapsed = (System.nanoTime() - smokeStartTime)/1000000;
            if(elapsed > 120){
                smoke.add(new Smokepuff(player.getX(), player.getY()+10));
                smokeStartTime = System.nanoTime();
            }

            for(int i = 0; i<smoke.size();i++)
            {
                smoke.get(i).update();
                if(smoke.get(i).getX()<-10)
                {
                    smoke.remove(i);
                }
            }
        }
        else{
            player.resetDY();
            if(!reset)
            {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                dissapear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion),player.getX(),
                        player.getY()-30, 100, 100, 25);
            }

            explosion.update();
            long resetElapsed = (System.nanoTime()-startReset)/1000000;

            if(resetElapsed > 2500 && !newGameCreated)
            {
                newGame();
            }


        }

    }
    public boolean collision(GameObject a, GameObject b)
    {
        if(Rect.intersects(a.getRectangle(), b.getRectangle()))
        {
            return true;
        }
        return false;
    }
    @Override
    public void draw(Canvas canvas)
    {
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);

        if(canvas!=null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if(!dissapear) {
                player.draw(canvas);
            }
            //draw smokepuffs
            for(Smokepuff sp: smoke)
            {
                sp.draw(canvas);
            }
            //draw missiles
            for(Missile m: missiles)
            {
                m.draw(canvas);
            }


            //draw topborder
            for(TopBorder tb: topborder)
            {
                tb.draw(canvas);
            }

            //draw botborder
            for(BotBorder bb: botborder)
            {
                bb.draw(canvas);
            }
            //draw explosion
            if(started)
            {
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);

        }
    }

    public void updateTopBorder()
    {
        // Mỗi 50 điểm, chèn đặt ngẫu nhiên khối hàng đầu mà phá vỡ các mô hình
        if(player.getScore()%50 ==0)
        {
            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick
            ),topborder.get(topborder.size()-1).getX()+20,0,(int)((rand.nextDouble()*(maxBorderHeight
            ))+1)));
        }
        for(int i = 0; i<topborder.size(); i++)
        {
            topborder.get(i).update();
            if(topborder.get(i).getX()<-20)
            {
                topborder.remove(i);
                // Xóa phần của ArrayList, thay thế nó bằng cách thêm một cái mới

                // Tính topdown mà xác định hướng biên giới đang chuyển động (lên hoặc xuống)
                if(topborder.get(topborder.size()-1).getHeight()>=maxBorderHeight)
                {
                    topDown = false;
                }
                if(topborder.get(topborder.size()-1).getHeight()<=minBorderHeight)
                {
                    topDown = true;
                }
                // Biên giới mới được thêm vào sẽ có chiều cao lớn hơn
                if(topDown)
                {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick),topborder.get(topborder.size()-1).getX()+40,
                            0, topborder.get(topborder.size()-1).getHeight()+1));
                }
                // Biên giới mới được thêm vào sẽ có chiều cao nhỏ hơn
                else
                {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick),topborder.get(topborder.size()-1).getX()+20,
                            0, topborder.get(topborder.size()-1).getHeight()-1));
                }

            }
        }

    }
    public void updateBottomBorder()
    {
        // mỗi 40 điểm, chèn đặt ngẫu nhiên mà phá vỡ mô hình
        if(player.getScore()%40 == 0)
        {
            botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botborder.get(botborder.size()-1).getX()+20,(int)((rand.nextDouble()
                    *maxBorderHeight)+(HEIGHT-maxBorderHeight))));
        }

        //update bottom border
        for(int i = 0; i<botborder.size(); i++)
        {
            botborder.get(i).update();

            // Nếu biên giới được di chuyển ra khỏi màn hình, loại bỏ nó và thêm một cái mới tương ứng
            if(botborder.get(i).getX()<-20) {
                botborder.remove(i);


                // Xác định biên giới sẽ được di chuyển lên hoặc xuống
                if (botborder.get(botborder.size() - 1).getY() <= HEIGHT-maxBorderHeight) {
                    botDown = true;
                }
                if (botborder.get(botborder.size() - 1).getY() >= HEIGHT - minBorderHeight) {
                    botDown = false;
                }

                if (botDown) {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                    ), botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1
                    ).getY() + 1));
                } else {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                    ), botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1
                    ).getY() - 1));
                }
            }
        }
    }
    public void newGame()
    {
        dissapear = false;

        botborder.clear();
        topborder.clear();

        missiles.clear();
        smoke.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;

        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT/2);

        if(player.getScore()>best)
        {
            best = player.getScore();

        }


// Tạo ra biên giới ban đầu

        // Biên giới đầu ban đầu
        for(int i = 0; i*20<WIDTH+40;i++)
        {
            // Biên giới đầu tiên tạo
            if(i==0)
            {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick
                ),i*20,0, 10));
            }
            else
            {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick
                ),i*20,0, topborder.get(i-1).getHeight()+1));
            }
        }
        // Biên giới phía dưới ban đầu
        for(int i = 0; i*20<WIDTH+40; i++)
        {
            // Khẩu đầu tiên từng được tạo ra
            if(i==0)
            {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick)
                        ,i*20,HEIGHT - minBorderHeight));
            }
            // Thêm các đường viền cho đến khi màn hình ban đầu là đệ
            else
            {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, botborder.get(i - 1).getY() - 1));
            }
        }

        newGameCreated = true;


    }
    public void drawText(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Điểm số: " + (player.getScore()*3), 10, HEIGHT - 10, paint);

        if(!player.getPlaying()&&newGameCreated&&reset)
        {
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("Giữ để tăng độ cao.", WIDTH/2-50, HEIGHT/2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("Hãy tránh tên lửa và tường.", WIDTH/2-50, HEIGHT/2 + 20, paint1);
            canvas.drawText("Hãy sẵn sàng..", WIDTH/2-50, HEIGHT/2 + 40, paint1);
        }
    }

}
