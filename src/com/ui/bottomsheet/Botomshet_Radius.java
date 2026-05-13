
package com.ui.bottomsheet;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

@DesignerComponent(
    version = 8,
    description = "Botomshet Khusus Tamoda V8. Transparan total, warna tetap ada, klik luar layar TIDAK tertutup, sangat hemat blok.",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = ""
)
@SimpleObject(external = true)
@UsesLibraries(libraries = "")
@UsesPermissions(permissionNames = "")

public class Botomshet_Radius extends AndroidNonvisibleComponent {

    private Context context;
    private Dialog bottomSheetDialog;
    private ViewGroup originalParent;
    private Handler uiHandler;

    // --- VARIABEL PROPERTI (WARNA & RADIUS AMAN) ---
    private int bgColor = Color.TRANSPARENT; 
    private float cornerRadius = 20f;
    private boolean isCancelable = false; // Default False agar back button tidak sembarangan tutup

    public Botomshet_Radius(ComponentContainer container) {
        super(container.$form());
        this.context = container.$context();
        this.uiHandler = new Handler(Looper.getMainLooper());
    }

    // =================================================================
    // 1. BLOK HIJAU (PENGATURAN WARNA, RADIUS & CANCELABLE)
    // =================================================================

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = "&H00000000")
    @SimpleProperty(description = "Warna background Bottom Sheet.")
    public void BackgroundColor(int color) {
        this.bgColor = color;
    }

    @SimpleProperty(description = "Ambil warna background saat ini.")
    public int BackgroundColor() {
        return this.bgColor;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "20.0")
    @SimpleProperty(description = "Lengkungan sudut atas (Radius).")
    public void Radius(float radius) {
        this.cornerRadius = radius;
    }

    @SimpleProperty
    public float Radius() {
        return this.cornerRadius;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(description = "Set TRUE jika ingin tombol Back HP bisa menutup menu ini.")
    public void Cancelable(boolean cancelable) {
        this.isCancelable = cancelable;
    }

    @SimpleProperty
    public boolean Cancelable() {
        return this.isCancelable;
    }


    // =================================================================
    // 2. BLOK UNGU UTAMA (SHOW & HIDE) SANGAT SIMPEL
    // =================================================================

    @SimpleFunction(description = "Munculkan Bottom Sheet. Hanya butuh Layout target.")
    public void Show(AndroidViewComponent layoutComponent) {
        final View view = layoutComponent.getView();

        view.setAlpha(1.0f); 

        if (view.getParent() != null) {
            originalParent = (ViewGroup) view.getParent();
            originalParent.removeView(view);
        }

        // IMPLEMENTASI WARNA & RADIUS (Sesuai Permintaan Bos)
        GradientDrawable bgShape = new GradientDrawable();
        bgShape.setColor(this.bgColor);
        float[] radii = {this.cornerRadius, this.cornerRadius, this.cornerRadius, this.cornerRadius, 0, 0, 0, 0};
        bgShape.setCornerRadii(radii);
        view.setBackground(bgShape);
        view.setVisibility(View.VISIBLE);

        bottomSheetDialog = new Dialog(context);
        bottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomSheetDialog.setContentView(view);
        
        // --- KUNCI SISTEM SPESIFIK TAMODA ---
        bottomSheetDialog.setCancelable(this.isCancelable); 
        bottomSheetDialog.setCanceledOnTouchOutside(false); // KUNCI MATI: Klik luar layar TIDAK akan menutup sheet!

        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.BOTTOM);

            // KUNCI TRANSPARAN & BEBAS KLIK BACKGROUND VIDEO
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }

        // LOGIKA KEMBALIKAN LAYOUT DENGAN AMAN
        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                view.setAlpha(0.0f);
                view.setVisibility(View.GONE);

                uiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (view.getParent() != null) {
                                ((ViewGroup) view.getParent()).removeView(view);
                            }
                            if (originalParent != null) {
                                originalParent.addView(view);
                                view.setVisibility(View.GONE); 
                            }
                            OnHidden(); // Panggil blok event kuning
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 50);
            }
        });

        bottomSheetDialog.show();
    }

    @SimpleFunction(description = "Sembunyikan / Tutup Bottom Sheet.")
    public void Hide() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
    }

    @SimpleFunction(description = "Cek apakah Bottom Sheet sedang terbuka.")
    public boolean IsShowing() {
        if (bottomSheetDialog != null) {
            return bottomSheetDialog.isShowing();
        }
        return false;
    }


    // =================================================================
    // 3. BLOK KUNING (EVENT)
    // =================================================================

    @SimpleEvent(description = "Event saat Bottom Sheet selesai ditutup dan layout aman dikembalikan.")
    public void OnHidden(){
        EventDispatcher.dispatchEvent(this, "OnHidden");
    }
}
