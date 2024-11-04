package dam.clases.monje_financiero_app.activities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import dam.clases.monje_financiero_app.R;

public class ALoadingDialog extends Dialog {
    private ImageView imageView;

    public ALoadingDialog(@NonNull Context context) {
        super(context);
        setCancelable(false);
        setOnCancelListener(null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View view = LayoutInflater.from(context).inflate(R.layout.loader_layout, null);
        setContentView(view);

        imageView = view.findViewById(R.id.loadingGifImageView);
        Glide.with(context)
                .asGif()
                .load(R.raw.loader_money) // Asegúrate de tener el GIF en la carpeta drawable
                .into(imageView);
    }
}
