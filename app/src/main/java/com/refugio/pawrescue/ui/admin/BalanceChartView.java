package com.refugio.pawrescue.ui.admin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class BalanceChartView extends View {
    private Paint paintIngresos;
    private Paint paintEgresos;
    private Paint paintText;
    private Paint paintTextSmall;
    private double totalDonaciones = 0;
    private double totalGastos = 0;

    public BalanceChartView(Context context) {
        super(context);
        init();
    }

    public BalanceChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BalanceChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintIngresos = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintIngresos.setColor(Color.parseColor("#4CAF50"));
        paintIngresos.setStyle(Paint.Style.FILL);

        paintEgresos = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintEgresos.setColor(Color.parseColor("#F44336"));
        paintEgresos.setStyle(Paint.Style.FILL);

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(32f);
        paintText.setTextAlign(Paint.Align.CENTER);

        paintTextSmall = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTextSmall.setColor(Color.parseColor("#666666"));
        paintTextSmall.setTextSize(28f);
        paintTextSmall.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * Establece los datos para el gráfico de balance
     * @param donaciones Total de donaciones (ingresos)
     * @param gastos Total de gastos (egresos)
     */
    public void setData(double donaciones, double gastos) {
        this.totalDonaciones = donaciones;
        this.totalGastos = Math.abs(gastos);
        invalidate(); // Redibuja la vista
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int padding = 60;

        // Si no hay datos, mostrar mensaje
        if (totalDonaciones == 0 && totalGastos == 0) {
            paintText.setTextSize(36f);
            canvas.drawText("Sin datos financieros", width / 2f, height / 2f, paintText);
            return;
        }

        // Calcular el máximo para escalar las barras
        double maxValue = Math.max(totalDonaciones, totalGastos);
        float maxHeight = height - padding * 2;

        // Calcular alturas proporcionales
        float ingresosHeight = (float) ((totalDonaciones / maxValue) * maxHeight);
        float egresosHeight = (float) ((totalGastos / maxValue) * maxHeight);

        // Ancho de cada barra
        float barWidth = (width - padding * 3) / 2f;

        // Posición X de cada barra
        float ingresosX = padding;
        float egresosX = padding * 2 + barWidth;

        // Dibujar barra de ingresos (desde abajo hacia arriba)
        RectF rectIngresos = new RectF(
                ingresosX,
                height - padding - ingresosHeight,
                ingresosX + barWidth,
                height - padding
        );
        canvas.drawRect(rectIngresos, paintIngresos);

        // Dibujar barra de egresos (desde abajo hacia arriba)
        RectF rectEgresos = new RectF(
                egresosX,
                height - padding - egresosHeight,
                egresosX + barWidth,
                height - padding
        );
        canvas.drawRect(rectEgresos, paintEgresos);

        // Dibujar etiquetas de texto
        paintTextSmall.setTextSize(24f);

        // Etiqueta "Ingresos"
        canvas.drawText("Ingresos",
                ingresosX + barWidth / 2,
                height - 15,
                paintTextSmall);

        // Etiqueta "Egresos"
        canvas.drawText("Egresos",
                egresosX + barWidth / 2,
                height - 15,
                paintTextSmall);

        // Dibujar valores sobre las barras
        paintText.setTextSize(22f);
        paintText.setColor(Color.WHITE);

        // Valor de ingresos
        String ingresosText = formatCurrency(totalDonaciones);
        canvas.drawText(ingresosText,
                ingresosX + barWidth / 2,
                height - padding - ingresosHeight + 35,
                paintText);

        // Valor de egresos
        String egresosText = formatCurrency(totalGastos);
        canvas.drawText(egresosText,
                egresosX + barWidth / 2,
                height - padding - egresosHeight + 35,
                paintText);

        // Restaurar color del texto
        paintText.setColor(Color.BLACK);
    }

    /**
     * Formatea un número como moneda
     */
    private String formatCurrency(double amount) {
        if (amount >= 1000000) {
            return String.format("$%.1fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("$%.1fK", amount / 1000);
        } else {
            return String.format("$%.0f", amount);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Asegurar un tamaño mínimo
        int minWidth = 200;
        int minHeight = 150;

        int width = Math.max(getSuggestedMinimumWidth(), minWidth);
        int height = Math.max(getSuggestedMinimumHeight(), minHeight);

        setMeasuredDimension(
                resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec)
        );
    }
}