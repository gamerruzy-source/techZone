package ph65249.fpoly.techzone;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {

    public static void show(Context context, String message) {
        show(context, message, R.drawable.iconapp);
    }

    public static void show(Context context, String message, int iconRes) {
        if (context == null) return;

        try {
            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(R.layout.layout_custom_toast, null);

            ImageView imgToastIcon = layout.findViewById(R.id.imgToastIcon);
            TextView tvToastMessage = layout.findViewById(R.id.tvToastMessage);

            if (imgToastIcon != null) {
                if (iconRes != 0) {
                    imgToastIcon.setImageResource(iconRes);
                } else {
                    imgToastIcon.setImageResource(R.drawable.iconapp);
                }
            }

            if (tvToastMessage != null) {
                tvToastMessage.setText(message);
            }

            Toast toast = new Toast(context.getApplicationContext());
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 160);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
        } catch (Exception e) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
