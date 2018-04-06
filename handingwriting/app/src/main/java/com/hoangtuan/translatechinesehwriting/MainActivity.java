package com.hoangtuan.translatechinesehwriting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.hoangtuan.translatechinesehwriting.Adapter.GoiYAdapter;

import com.hoangtuan.translatechinesehwriting.Model.GoiYModel;
import com.myscript.atk.core.ui.IStroker;
import com.myscript.atk.scw.SingleCharWidgetApi;
import com.myscript.atk.text.CandidateInfo;


import java.util.ArrayList;
import java.util.List;

import static com.myscript.atk.scw.SingleCharWidgetApi.INK_FADE_OUT_ON_PEN_UP;

public class MainActivity extends Activity implements
        SingleCharWidgetApi.OnConfiguredListener,
        SingleCharWidgetApi.OnTextChangedListener,
        SingleCharWidgetApi.OnSingleTapGestureListener,
        SingleCharWidgetApi.OnLongPressGestureListener,
        SingleCharWidgetApi.OnBackspaceGestureListener,
        SingleCharWidgetApi.OnReturnGestureListener,
        CustomEdittext.OnSelectionChanged {

    private Toolbar tBar;
    private MaterialMenuDrawable materialMenu;
    private ImageView imgCopy;
    private CustomEdittext mTextField;
    private SingleCharWidgetApi mWidget;
    private ArrayList<GoiYModel> goiYModels;
    private GoiYAdapter goiYAdapter;
    private RecyclerView recyGoiY;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private ImageButton imgPen;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialization
        goiYModels = new ArrayList<>();
        goiYAdapter = new GoiYAdapter(this);

        //findByID
        imgPen = (ImageButton) findViewById(R.id.imgPen);
        imgCopy = (ImageView) findViewById(R.id.imgCopy);
        tBar = (Toolbar) findViewById(R.id.tBar);
        mWidget = (SingleCharWidgetApi) findViewById(R.id.singChar);
        mTextField = (CustomEdittext) findViewById(R.id.edtText);
        recyGoiY = (RecyclerView) findViewById(R.id.recyGoiY);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.naviView);

        //setEvent


        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.REGULAR);
        materialMenu.setIconState(MaterialMenuDrawable.IconState.BURGER);

        imgPen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPencilButtonClick(v);
            }
        });

        tBar.setNavigationIcon(materialMenu);
        tBar.setTitleTextColor(Color.WHITE);
        tBar.setTitle(getResources().getString(R.string.home));
        tBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT, true);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.menuHome:
                        drawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                    case R.id.menuTaiDuLieu:
                        Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.menuThemUngDung:
                        Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.menuChiaSe:
                        Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.menuDanhGia:
                        Uri uri = Uri.parse("market://details?id=com.diffcat.facedance2");
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        // To count with Play market backstack, After pressing back button,
                        // to taken back to our application, we need to add following flags to intent.
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        try {
                            startActivity(goToMarket);
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=com.diffcat.facedance2"

                                    )));
                        }
                        Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.menuAbout:
                        Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.menuCopyR:
                        Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });

        if (!mWidget.registerCertificate(Khoa.getBytes())) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Cần thêm chứng chỉ");
            dlgAlert.setTitle("Thiếu chứng chỉ");
            dlgAlert.setCancelable(false);
            dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlgAlert.create().show();
            return;
        }

        mWidget.setOnConfiguredListener(this);
        mWidget.setOnTextChangedListener(this);
        mWidget.setOnBackspaceGestureListener(this);
        mWidget.setOnReturnGestureListener(this);
        mWidget.setOnSingleTapGestureListener(this);
        mWidget.setOnLongPressGestureListener(this);
        //OnlyChinese
        mWidget.setAutoSpaceEnabled(true);
        mWidget.setInkFadeOutEffect(INK_FADE_OUT_ON_PEN_UP);
        mWidget.setInkFadeOutDelay(INK_FADE_OUT_ON_PEN_UP);

        mWidget.setInkColor(getResources().getColor(R.color.colorTongThe));


        Log.d("pack", getPackageCodePath());
//        mWidget.addSearchDir("zip://" + getPackageCodePath() + "!/assets/conf");
        mWidget.addSearchDir(Environment.getExternalStorageDirectory() + "/"
                + "conf");
        mWidget.configure("zh_CN", "iso_text");

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyGoiY.setHasFixedSize(true);
        recyGoiY.setLayoutManager(manager);
        recyGoiY.setAdapter(goiYAdapter);
        recyGoiY.addOnItemTouchListener(new RecyclerItemClickListener(this, recyGoiY, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                GoiYModel tag = goiYModels.get(position);
                mWidget.replaceCharacters(tag.getStart(), tag.getEnd(), tag.getText());
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

        imgCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClipboard(getApplicationContext(), mTextField.getText().toString().trim());
                Toast.makeText(MainActivity.this, "Đã copy", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWidget.dispose();
    }

    private void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void onSelectionChanged(EditText editText, int selStart, int selEnd) {
        mWidget.setInsertIndex(selStart);
        updateCandidatePanel();
    }

    @Override
    public void onBackspaceGesture(SingleCharWidgetApi singleCharWidgetApi, int i, int i1) {

        CandidateInfo info = mWidget.getCharacterCandidates(i - 1);
        mWidget.replaceCharacters(info.getStart(), info.getEnd(), null);
    }

    @Override
    public void onConfigured(SingleCharWidgetApi singleCharWidgetApi, boolean b) {

    }

    @Override
    public boolean onLongPressGesture(SingleCharWidgetApi singleCharWidgetApi, float v, float v1) {
        return false;
    }

    @Override
    public void onReturnGesture(SingleCharWidgetApi singleCharWidgetApi, int i) {
        mWidget.replaceCharacters(i, i, "\n");
    }

    @Override
    public boolean onSingleTapGesture(SingleCharWidgetApi singleCharWidgetApi, float v, float v1) {
        return false;
    }

    @Override
    public void onTextChanged(SingleCharWidgetApi singleCharWidgetApi, String s, boolean b) {
        mTextField.setOnSelectionChangedListener(null);
        mTextField.setTextKeepState(s);
        mTextField.setSelection(mWidget.getInsertIndex());
        mTextField.setOnSelectionChangedListener(this);
        updateCandidatePanel();
    }

    public void onDelete(View view) {
        CandidateInfo info = mWidget.getCharacterCandidates(mWidget.getInsertIndex() - 1);
        mWidget.replaceCharacters(info.getStart(), info.getEnd(), null);

    }

    public void onSpace(View view) {
        mWidget.insertString(" ");
    }

    public void onClear(View view) {
        mWidget.clear();

    }

    public void onFind(View view) {
        String text = mTextField.getText().toString().trim();
        if (!text.equals("")) {
            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            intent.putExtra("text", mTextField.getText().toString());

            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    private void updateCandidatePanel() {

        goiYModels.clear();
        int index = mWidget.getInsertIndex() - 1;
        if (index < 0) {
            index = 0;
        }

        CandidateInfo[] infos = {
                mWidget.getWordCandidates(index),
                mWidget.getCharacterCandidates(index),
        };

        for (CandidateInfo info : infos) {
            int start = info.getStart();
            int end = info.getEnd();
            List<String> labels = info.getLabels();
            List<String> completions = info.getCompletions();

            for (int i = 0; i < labels.size(); i++) {
                GoiYModel tag = new GoiYModel();
                tag.setStart(start);
                tag.setEnd(end);
                tag.setText(labels.get(i) + completions.get(i));
                goiYModels.add(tag);

            }
            goiYAdapter.setCandidates(goiYModels);

        }
    }

    //Pencil
    public void onPencilButtonClick(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);

        popupMenu.getMenu().add(Menu.NONE, IStroker.Stroking.FELT_PEN.ordinal(), Menu.NONE, R.string.effect_felt_pen);
        popupMenu.getMenu().add(Menu.NONE, IStroker.Stroking.FOUNTAIN_PEN.ordinal(), Menu.NONE, R.string.effect_fountain_pen);
        popupMenu.getMenu().add(Menu.NONE, IStroker.Stroking.CALLIGRAPHIC_BRUSH.ordinal(), Menu.NONE, R.string.effect_calligraphic_brush);
        popupMenu.getMenu().add(Menu.NONE, IStroker.Stroking.CALLIGRAPHIC_QUILL.ordinal(), Menu.NONE, R.string.effect_calligraphic_quill);
        popupMenu.getMenu().add(Menu.NONE, IStroker.Stroking.QALAM.ordinal(), Menu.NONE, R.string.effect_qalam);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mWidget.setInkEffect(IStroker.Stroking.values()[item.getItemId()]);
                return true;
            }
        });

        popupMenu.show();
    }





}
