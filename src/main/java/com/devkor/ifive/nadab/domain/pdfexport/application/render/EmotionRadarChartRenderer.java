package com.devkor.ifive.nadab.domain.pdfexport.application.render;

import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent.EmotionStat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 월간 감정 레이더. 격자+폴리곤만 Java2D PNG, 축 라벨은 위치만 반환해 어셈블러가 HTML 텍스트로 배치(선명·폰트 사본 불필요).
 * 동심원 4단·시작각 -90°·현재/이전 2계열(둘 다 실선)·계열 3개 미만이면 미렌더.
 */
@Slf4j
@Component
public class EmotionRadarChartRenderer {

    /** 표시 기준 좌표(240 캔버스·R78)에 배율을 곱해 고해상 PNG로 굽고 CSS로 축소(얇은 선 계단 아티팩트 완화). */
    private static final int SCALE = 3;
    private static final int CHART = 240;                 // 표시 px(= HTML 컨테이너 크기)
    private static final int VIEW = CHART * SCALE;        // PNG 캔버스 px
    private static final double CENTER = VIEW / 2.0;
    private static final double RADIUS = 78 * SCALE;
    private static final int GRID_STEPS = 4;
    private static final double LABEL_RADIUS = 104;       // 표시 px, 중심에서 라벨 중심까지
    private static final float SERIES_STROKE = 1.07f;     // 데이터 폴리곤 선 두께(표시 px)
    private static final float GRID_STROKE = 0.54f;       // 동심원·축선 두께(표시 px)

    private static final Color GRID = Color.decode("#E8EBF2");
    private static final Color CURRENT_STROKE = Color.decode("#5D57F6");
    private static final Color CURRENT_FILL = new Color(93, 87, 246, 0x33);   // brand @20%
    private static final Color PREVIOUS_STROKE = Color.decode("#F657E6");
    private static final Color PREVIOUS_FILL = new Color(246, 87, 230, 0x40); // #F657E6 @25%

    /** 차트 PNG(data URI) + 축 라벨(어셈블러가 HTML 텍스트로 렌더). */
    public record RadarChart(String imageDataUri, List<RadarLabel> labels) {
    }

    /** 라벨 1개: 감정명·퍼센트 + 컨테이너(240×240) 내 중심 좌표(표시 px). */
    public record RadarLabel(String name, int percent, int x, int y) {
    }

    /** 현재/이전(nullable) 감정 stats → 차트. 렌더 불가(계열<3)면 empty. */
    public Optional<RadarChart> render(List<EmotionStat> current, List<EmotionStat> previous) {
        List<Axis> axes = toAxes(current);
        if (axes.size() < 3) {
            return Optional.empty();
        }

        Map<String, Integer> previousByCode = new HashMap<>();
        if (previous != null) {
            for (EmotionStat stat : previous) {
                if (stat != null && stat.emotionCode() != null && stat.percent() != null) {
                    previousByCode.put(stat.emotionCode(), stat.percent());
                }
            }
        }

        int max = 1;
        for (Axis a : axes) {
            max = Math.max(max, a.percent);
            max = Math.max(max, previousByCode.getOrDefault(a.code, 0));
        }
        boolean hasPrevious = axes.stream().anyMatch(a -> previousByCode.getOrDefault(a.code, 0) > 0);

        // 불투명 흰 배경(레이더는 항상 흰 박스 위) → 투명 픽셀 검정 RGB로 인한 다운샘플 다크 프린지 제거.
        BufferedImage img = new BufferedImage(VIEW, VIEW, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, VIEW, VIEW);

            drawGrid(g, axes.size());
            if (hasPrevious) {
                drawPolygon(g, axes, previousByCode, max, PREVIOUS_FILL, PREVIOUS_STROKE);
            }
            drawValuePolygon(g, axes, max);
        } finally {
            g.dispose();
        }

        Optional<String> uri = encode(img);
        return uri.map(dataUri -> new RadarChart(dataUri, labels(axes)));
    }

    /** 각 축 라벨의 컨테이너 내 중심 좌표(표시 px). index 0 을 위(-90°)에 두고 시계방향. */
    private List<RadarLabel> labels(List<Axis> axes) {
        List<RadarLabel> out = new ArrayList<>();
        int total = axes.size();
        for (int i = 0; i < total; i++) {
            double angle = -Math.PI / 2 + 2 * Math.PI * i / total;
            int x = (int) Math.round(CHART / 2.0 + LABEL_RADIUS * Math.cos(angle));
            int y = (int) Math.round(CHART / 2.0 + LABEL_RADIUS * Math.sin(angle));
            Axis a = axes.get(i);
            out.add(new RadarLabel(a.label, a.percent, x, y));
        }
        return out;
    }

    private void drawGrid(Graphics2D g, int total) {
        g.setColor(GRID);
        g.setStroke(new BasicStroke(GRID_STROKE * SCALE));
        for (int step = 1; step <= GRID_STEPS; step++) {
            double r = RADIUS / GRID_STEPS * step;
            g.drawOval((int) (CENTER - r), (int) (CENTER - r), (int) (r * 2), (int) (r * 2));
        }
        for (int i = 0; i < total; i++) {
            double[] p = point(i, total, RADIUS);
            g.drawLine((int) CENTER, (int) CENTER, (int) p[0], (int) p[1]);
        }
    }

    private void drawValuePolygon(Graphics2D g, List<Axis> axes, int max) {
        Path2D path = new Path2D.Double();
        for (int i = 0; i < axes.size(); i++) {
            double[] p = point(i, axes.size(), axes.get(i).percent / (double) max * RADIUS);
            if (i == 0) {
                path.moveTo(p[0], p[1]);
            } else {
                path.lineTo(p[0], p[1]);
            }
        }
        path.closePath();
        g.setColor(CURRENT_FILL);
        g.fill(path);
        g.setColor(CURRENT_STROKE);
        g.setStroke(new BasicStroke(SERIES_STROKE * SCALE));
        g.draw(path);
    }

    private void drawPolygon(Graphics2D g, List<Axis> axes, Map<String, Integer> byCode, int max,
                            Color fill, Color stroke) {
        Path2D path = new Path2D.Double();
        for (int i = 0; i < axes.size(); i++) {
            int percent = byCode.getOrDefault(axes.get(i).code, 0);
            double[] p = point(i, axes.size(), percent / (double) max * RADIUS);
            if (i == 0) {
                path.moveTo(p[0], p[1]);
            } else {
                path.lineTo(p[0], p[1]);
            }
        }
        path.closePath();
        g.setColor(fill);
        g.fill(path);
        g.setColor(stroke);
        g.setStroke(new BasicStroke(SERIES_STROKE * SCALE));
        g.draw(path);
    }

    /** index 0 을 위(-90°)에 두고 시계방향. */
    private double[] point(int index, int total, double radius) {
        double angle = -Math.PI / 2 + 2 * Math.PI * index / total;
        return new double[]{CENTER + radius * Math.cos(angle), CENTER + radius * Math.sin(angle)};
    }

    private List<Axis> toAxes(List<EmotionStat> current) {
        List<Axis> axes = new ArrayList<>();
        if (current == null) {
            return axes;
        }
        for (EmotionStat stat : current) {
            if (stat == null || stat.percent() == null) {
                continue;
            }
            String code = stat.emotionCode();
            String label = stat.emotionName() != null ? stat.emotionName() : PdfEmotionPalette.of(code).label();
            axes.add(new Axis(code, label, stat.percent()));
        }
        return axes;
    }

    private Optional<String> encode(BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return Optional.of("data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray()));
        } catch (IOException e) {
            log.warn("[PDF_EXPORT] 감정 레이더 PNG 인코딩 실패 — 차트 생략", e);
            return Optional.empty();
        }
    }

    private record Axis(String code, String label, int percent) {
    }
}