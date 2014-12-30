package com.pipit.waffle;

import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Kyle on 11/19/2014.
 */
public class AnsweringFragment extends Fragment implements SpringListener {

    private static double TENSION = 800;
    private static double DAMPER = 20; //friction

    private CardView cardViewTop1;
    private CardView cardViewBot1;
    private CardView cardViewTop2;
    private CardView cardViewBot2;
    private SpringSystem mSpringSystem;
    private Spring mSpring;

    private VelocityTracker velocity = null;

    private boolean mMovedUp = false;
    private float mOrigY;
    private float mOrigX;

    private float upper_card_x_pos;
    private float lower_card_x_pos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Outline

        View v = inflater.inflate(R.layout.answering_fragment, container, false);

        cardViewTop1 = (CardView) v.findViewById(R.id.card_view);
        cardViewBot1 = (CardView) v.findViewById(R.id.card_view2);

        cardViewTop2 = (CardView) v.findViewById(R.id.card_view_extra);
        cardViewBot2 = (CardView) v.findViewById(R.id.card_view2_extra);

        // Set the CardViews' size and margins
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;
        int height = size.y;

        int margin = (int) (8 * getActivity().getResources().getDisplayMetrics().density);

        int margin_left = (int) (1000 * getActivity().getResources().getDisplayMetrics().density);

        final int frame_width = (int) (2000 * getActivity().getResources().getDisplayMetrics().density);


        CardView.LayoutParams card_params = (CardView.LayoutParams) cardViewTop1.getLayoutParams();
        card_params.width = width - (2*margin);

        final int starting_pos = margin_left - (card_params.width/2);
        final int ending_pos = margin_left + (card_params.width/2) + 24;
        final float ending_pos_left = margin_left - ((3.0f /2.0f) * (float) card_params.width) -24;


        card_params.setMargins(margin_left - (card_params.width/2), margin, margin, margin);

        cardViewTop1.setLayoutParams(card_params);

        CardView.LayoutParams card_params2 = (CardView.LayoutParams) cardViewBot1.getLayoutParams();
        card_params2.width = width - (2*margin);
        card_params2.setMargins(margin_left - (card_params.width / 2), 0, margin, margin);

        cardViewBot1.setLayoutParams(card_params2);


        CardView.LayoutParams card_params3 = (CardView.LayoutParams) cardViewTop2.getLayoutParams();
        card_params3.width = width - (2*margin);
        card_params3.setMargins(0, margin, margin, margin);

        cardViewTop2.setLayoutParams(card_params3);

        CardView.LayoutParams card_params4 = (CardView.LayoutParams) cardViewBot2.getLayoutParams();
        card_params4.width = width - (2*margin);
        card_params4.setMargins(0, 0, margin, margin);

        cardViewBot2.setLayoutParams(card_params4);






        mSpringSystem = SpringSystem.create();

        mSpring = mSpringSystem.createSpring();
        mSpring.addListener(this);

        SpringConfig config = new SpringConfig(TENSION, DAMPER);
        mSpring.setSpringConfig(config);

        Resources r = getResources();
        final float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        //mSpring.setEndValue(cardViewTop1.getX());

         //CardView movement
        final View.OnTouchListener tl = new View.OnTouchListener() {
            public float offsetX;
            public float offsetY;
            private ArrayList<Float> last_velocities = new ArrayList<Float>(3);
            private boolean selected = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int theAction = event.getAction();

                int index = event.getActionIndex();
                int action = event.getActionMasked();
                int pointerId = event.getPointerId(index);



                switch (theAction) {
                    case MotionEvent.ACTION_DOWN:
                        // Button down
                        last_velocities.clear();

                        last_velocities.add(0, 0.0f);
                        last_velocities.add(1, 0.0f);
                        last_velocities.add(2, 0.0f);

                        if(velocity == null) {
                            // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                            velocity = VelocityTracker.obtain();
                        }
                        else {
                            // Reset the velocity tracker back to its initial state.
                            velocity.clear();
                        }

                        mOrigX = v.getX();
                        mOrigY = v.getY();
                        offsetX = v.getX() - event.getRawX();
                        offsetY = v.getY() - event.getRawY();



                        velocity.addMovement(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // Button moved
                        velocity.addMovement(event);
                        velocity.computeCurrentVelocity(1000);
                        float current_vel = VelocityTrackerCompat.getXVelocity(velocity,
                                pointerId);
                        Log.d("AnsweringFragment", "Velocity: " + current_vel);

                        // Remember the last 3 velocities
                        last_velocities.set(2, last_velocities.get(1));
                        last_velocities.set(1, last_velocities.get(0));
                        last_velocities.set(0, current_vel);


                        //Log.d("AnsweringFragment", Float.toString(x_velocity));
                        float newX = event.getRawX() + offsetX;
                        float newY = event.getRawY() + offsetY;
                        //WindowManager wm = (WindowManager) v.getSystemService(Context.WINDOW_SERVICE);
                        // Display display = wm.getDefaultDisplay();
                        //DisplayMetrics metrics = new DisplayMetrics();
                        // display.getMetrics(metrics);
                        //int width = metrics.widthPixels;
                        //int height = metrics.heightPixels;
                        if (!(newX < starting_pos - 10))
                            v.setX(newX);


                        break;
                    case MotionEvent.ACTION_UP:
                        // Button up

                        // Currently, the options for a "choice selection" are:
                        // 1) a velocity of 10,000 and at least 1/2 of the screen width in the positive
                        // x direction on ACTION_UP
                        // 2) at least 4/7 of the screen width in the positive x direction on
                        // ACTION_UP
                        // For case 2, we will (plan to - TODO) release the CardView at some set velocity
                        // For case 1, we will (plan to - TODO) release the CardView at some velocity near
                        // max_vel and slow it down as it approaches the right edge of the screen

                        Float max_vel = Collections.max(last_velocities);

                        Log.d("AnsweringFragment", "Max velocity: " + Float.toString(max_vel));

                        float xValue = v.getX();
                        // float yValue = v.getY();

                        float halfway = (float) frame_width / 2.0f;

                        float dist;
                        if(((xValue > halfway && (max_vel > 12000))) ||
                                (xValue > ((51.0f/100.0f)* (float) frame_width)))  {
                            Log.d("AnsweringFragment", "Selected! Released at " + Float.toString(xValue) + " pixels with a " +
                                    "velocity of " + Float.toString(max_vel) + " pixels per second.");
                            dist = ending_pos - xValue;
                            selected = true;
                            cardViewTop2.setX(ending_pos_left);
                            cardViewBot2.setX(ending_pos);
                        }
                        else
                        {
                            dist = starting_pos - xValue;

                        }




                        TranslateAnimation anim_in = new TranslateAnimation(0, starting_pos - ending_pos_left, 0, 0);
                        anim_in.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                cardViewTop2.setX(starting_pos);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        anim_in.setInterpolator(new DecelerateInterpolator(1.5f));

                        TranslateAnimation anim_in_right = new TranslateAnimation(0, starting_pos - ending_pos, 0, 0);

                        anim_in_right.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                cardViewBot2.setX(starting_pos);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        TranslateAnimation anim_other = new TranslateAnimation(0, ending_pos_left - cardViewBot1.getX() , 0, 0);
                        anim_other.setInterpolator(new DecelerateInterpolator(1.5f));

                        TranslateAnimation anim = new TranslateAnimation(0, dist, 0, 0);
                        anim.setInterpolator(new DecelerateInterpolator(1.5f));
                        anim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {

                                if(selected)
                                    cardViewTop1.setX(ending_pos);
                                else
                                    cardViewTop1.setX(starting_pos);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        //anim.setFillAfter(true);
                        anim.setFillEnabled(true);
                        anim_in.setFillEnabled(true);
                        anim_in_right.setFillEnabled(true);
                        anim_other.setFillEnabled(true);

                        // magical custom formula for creating appropriate slide speeds varying by travel distance
                        long dur = (long) ((dist/10) * (dist/10))/2;
                        if(dur > 500)
                            dur = 500;
                        if(dur < 200)
                            dur = 200;

                        anim.setDuration(dur);
                        anim_other.setDuration(dur);
                        anim_in.setDuration(dur);
                        anim_in_right.setDuration(dur);
                        anim_other.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                cardViewBot1.setX(ending_pos_left);
                            }


                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        anim_other.setStartOffset(300);
                        anim_in.setStartOffset(300);
                        anim_in_right.setStartOffset(300);
                        cardViewTop1.startAnimation(anim);
                        if(selected) {
                            cardViewBot1.startAnimation(anim_other);
                            cardViewTop2.startAnimation(anim_in);
                            cardViewBot2.startAnimation(anim_in_right);
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        };

        final View.OnTouchListener tl2 = new View.OnTouchListener() {
            public float offsetX;
            public float offsetY;
            private ArrayList<Float> last_velocities = new ArrayList<Float>(3);
            private boolean selected = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int theAction = event.getAction();

                int index = event.getActionIndex();
                int action = event.getActionMasked();
                int pointerId = event.getPointerId(index);



                switch (theAction) {
                    case MotionEvent.ACTION_DOWN:
                        // Button down
                        last_velocities.clear();

                        last_velocities.add(0, 0.0f);
                        last_velocities.add(1, 0.0f);
                        last_velocities.add(2, 0.0f);

                        if(velocity == null) {
                            // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                            velocity = VelocityTracker.obtain();
                        }
                        else {
                            // Reset the velocity tracker back to its initial state.
                            velocity.clear();
                        }

                        mOrigX = v.getX();
                        mOrigY = v.getY();
                        offsetX = v.getX() - event.getRawX();
                        offsetY = v.getY() - event.getRawY();

                        velocity.addMovement(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // Button moved
                        velocity.addMovement(event);
                        velocity.computeCurrentVelocity(1000);
                        float current_vel = VelocityTrackerCompat.getXVelocity(velocity,
                                pointerId);
                        Log.d("AnsweringFragment", "Velocity: " + current_vel);

                        // Remember the last 3 velocities
                        last_velocities.set(2, last_velocities.get(1));
                        last_velocities.set(1, last_velocities.get(0));
                        last_velocities.set(0, current_vel);


                        //Log.d("AnsweringFragment", Float.toString(x_velocity));
                        float newX = event.getRawX() + offsetX;
                        float newY = event.getRawY() + offsetY;
                        //WindowManager wm = (WindowManager) v.getSystemService(Context.WINDOW_SERVICE);
                        // Display display = wm.getDefaultDisplay();
                        //DisplayMetrics metrics = new DisplayMetrics();
                        // display.getMetrics(metrics);
                        //int width = metrics.widthPixels;
                        //int height = metrics.heightPixels;
                        if (!(newX < starting_pos - 10))
                            v.setX(newX);
                        //v.setY(newY);
                        break;
                    case MotionEvent.ACTION_UP:
                        // Button up

                        // Currently, the options for a "choice selection" are:
                        // 1) a velocity of 10,000 and at least 1/2 of the screen width in the positive
                        // x direction on ACTION_UP
                        // 2) at least 4/7 of the screen width in the positive x direction on
                        // ACTION_UP
                        // For case 2, we will (plan to - TODO) release the CardView at some set velocity
                        // For case 1, we will (plan to - TODO) release the CardView at some velocity near
                        // max_vel and slow it down as it approaches the right edge of the screen

                        Float max_vel = Collections.max(last_velocities);

                        Log.d("AnsweringFragment", "Max velocity: " + Float.toString(max_vel));

                        float xValue = v.getX();
                        // float yValue = v.getY();

                        float halfway = (float) frame_width / 2.0f;

                        float dist;
                        if(((xValue > halfway && (max_vel > 12000))) ||
                                (xValue > ((51.0f/100.0f)* (float) frame_width)))  {
                            Log.d("AnsweringFragment", "Selected! Released at " + Float.toString(xValue) + " pixels with a " +
                                    "velocity of " + Float.toString(max_vel) + " pixels per second.");
                            dist = ending_pos - xValue;
                            selected = true;
                            cardViewBot2.setX(ending_pos_left);
                            cardViewTop2.setX(ending_pos);
                        }
                        else
                        {
                            dist = starting_pos - xValue;
                        }

                        TranslateAnimation anim_in = new TranslateAnimation(0, starting_pos - ending_pos_left, 0, 0);
                        anim_in.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                cardViewBot2.setX(starting_pos);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        anim_in.setInterpolator(new DecelerateInterpolator(1.5f));

                        TranslateAnimation anim_in_right = new TranslateAnimation(0, starting_pos - ending_pos, 0, 0);

                        anim_in_right.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                cardViewTop2.setX(starting_pos);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        TranslateAnimation anim_other = new TranslateAnimation(0, ending_pos_left - cardViewTop1.getX() , 0, 0);
                        anim_other.setInterpolator(new DecelerateInterpolator(1.5f));

                        TranslateAnimation anim = new TranslateAnimation(0, dist, 0, 0);
                        anim.setInterpolator(new DecelerateInterpolator(1.5f));
                        anim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {

                                if(selected)
                                    cardViewBot1.setX(ending_pos);
                                else
                                    cardViewBot1.setX(starting_pos);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        //anim.setFillAfter(true);
                        anim.setFillEnabled(true);
                        anim_in.setFillEnabled(true);
                        anim_in_right.setFillEnabled(true);
                        anim_other.setFillEnabled(true);

                        // magical custom formula for creating appropriate slide speeds varying by travel distance
                        long dur = (long) ((dist/10) * (dist/10))/2;
                        if(dur > 500)
                            dur = 500;
                        if(dur < 200)
                            dur = 200;

                        anim.setDuration(dur);
                        anim_other.setDuration(dur);
                        anim_in.setDuration(dur);
                        anim_in_right.setDuration(dur);
                        anim_other.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                cardViewTop1.setX(ending_pos_left);
                            }


                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        anim_other.setStartOffset(300);
                        anim_in.setStartOffset(300);
                        anim_in_right.setStartOffset(300);
                        cardViewBot1.startAnimation(anim);
                        if(selected) {
                            cardViewTop1.startAnimation(anim_other);
                            cardViewBot2.startAnimation(anim_in);
                            cardViewTop2.startAnimation(anim_in_right);
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }


        };

        cardViewTop1.setOnTouchListener(tl);
        cardViewBot1.setOnTouchListener(tl2);


        return v;
    }


    @Override
    public void onSpringUpdate(Spring spring) {
        float value = (float) spring.getCurrentValue();

       cardViewTop1.setX(value);
    }

    @Override
    public void onSpringAtRest(Spring spring) {

    }

    @Override
    public void onSpringActivate(Spring spring) {

    }

    @Override
    public void onSpringEndStateChange(Spring spring) {

    }

}
