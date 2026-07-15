package com.chantalbortolussi.tattooapp.ui.components

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Rect

object TattooShapes {

    fun getCompassPath(width: Float, height: Float): Path {
        val path = Path()
        val cx = width / 2f
        val cy = height / 2f
        val r = Math.min(width, height) / 2f
        
        // Outer decorative ring
        path.addOval(Rect(cx - r * 0.85f, cy - r * 0.85f, cx + r * 0.85f, cy + r * 0.85f))
        
        // Inner divider ring
        path.addOval(Rect(cx - r * 0.65f, cy - r * 0.65f, cx + r * 0.65f, cy + r * 0.65f))
        
        // North Arrow
        path.moveTo(cx, cy)
        path.lineTo(cx - r * 0.12f, cy)
        path.lineTo(cx, cy - r * 0.8f)
        path.close()
        path.moveTo(cx, cy)
        path.lineTo(cx + r * 0.12f, cy)
        path.lineTo(cx, cy - r * 0.8f)
        path.close()

        // South Arrow
        path.moveTo(cx, cy)
        path.lineTo(cx - r * 0.12f, cy)
        path.lineTo(cx, cy + r * 0.8f)
        path.close()
        path.moveTo(cx, cy)
        path.lineTo(cx + r * 0.12f, cy)
        path.lineTo(cx, cy + r * 0.8f)
        path.close()

        // East Arrow
        path.moveTo(cx, cy)
        path.lineTo(cx, cy - r * 0.12f)
        path.lineTo(cx + r * 0.8f, cy)
        path.close()
        path.moveTo(cx, cy)
        path.lineTo(cx, cy + r * 0.12f)
        path.lineTo(cx + r * 0.8f, cy)
        path.close()

        // West Arrow
        path.moveTo(cx, cy)
        path.lineTo(cx, cy - r * 0.12f)
        path.lineTo(cx - r * 0.8f, cy)
        path.close()
        path.moveTo(cx, cy)
        path.lineTo(cx, cy + r * 0.12f)
        path.lineTo(cx - r * 0.8f, cy)
        path.close()

        // NE Arrow (shorter)
        path.moveTo(cx, cy)
        path.lineTo(cx - r * 0.05f, cy - r * 0.05f)
        path.lineTo(cx + r * 0.45f, cy - r * 0.45f)
        path.close()
        path.moveTo(cx, cy)
        path.lineTo(cx + r * 0.05f, cy + r * 0.05f)
        path.lineTo(cx + r * 0.45f, cy - r * 0.45f)
        path.close()

        // NW Arrow (shorter)
        path.moveTo(cx, cy)
        path.lineTo(cx + r * 0.05f, cy - r * 0.05f)
        path.lineTo(cx - r * 0.45f, cy - r * 0.45f)
        path.close()
        path.moveTo(cx, cy)
        path.lineTo(cx - r * 0.05f, cy + r * 0.05f)
        path.lineTo(cx - r * 0.45f, cy - r * 0.45f)
        path.close()

        // SE Arrow
        path.moveTo(cx, cy)
        path.lineTo(cx - r * 0.05f, cy + r * 0.05f)
        path.lineTo(cx + r * 0.45f, cy + r * 0.45f)
        path.close()
        path.moveTo(cx, cy)
        path.lineTo(cx + r * 0.05f, cy - r * 0.05f)
        path.lineTo(cx + r * 0.45f, cy + r * 0.45f)
        path.close()

        // SW Arrow
        path.moveTo(cx, cy)
        path.lineTo(cx + r * 0.05f, cy + r * 0.05f)
        path.lineTo(cx - r * 0.45f, cy + r * 0.45f)
        path.close()
        path.moveTo(cx, cy)
        path.lineTo(cx - r * 0.05f, cy - r * 0.05f)
        path.lineTo(cx - r * 0.45f, cy + r * 0.45f)
        path.close()

        return path
    }

    fun getLotusPath(width: Float, height: Float): Path {
        val path = Path()
        val cx = width / 2f
        val cy = height / 2f + height * 0.1f // low-center base
        
        // Base Central Petal
        path.moveTo(cx, cy)
        path.cubicTo(cx - width * 0.15f, cy - height * 0.2f, cx - width * 0.1f, cy - height * 0.6f, cx, cy - height * 0.7f)
        path.cubicTo(cx + width * 0.1f, cy - height * 0.6f, cx + width * 0.15f, cy - height * 0.2f, cx, cy)
        path.close()

        // Left Internal Petal
        path.moveTo(cx, cy)
        path.cubicTo(cx - width * 0.2f, cy - height * 0.1f, cx - width * 0.35f, cy - height * 0.4f, cx - width * 0.2f, cy - height * 0.55f)
        path.cubicTo(cx - width * 0.1f, cy - height * 0.45f, cx, cy - height * 0.3f, cx, cy)
        path.close()

        // Right Internal Petal
        path.moveTo(cx, cy)
        path.cubicTo(cx + width * 0.2f, cy - height * 0.1f, cx + width * 0.35f, cy - height * 0.4f, cx + width * 0.2f, cy - height * 0.55f)
        path.cubicTo(cx + width * 0.1f, cy - height * 0.45f, cx, cy - height * 0.3f, cx, cy)
        path.close()

        // Left External Petal (Drooping low)
        path.moveTo(cx, cy)
        path.cubicTo(cx - width * 0.35f, cy, cx - width * 0.45f, cy - height * 0.2f, cx - width * 0.38f, cy - height * 0.35f)
        path.cubicTo(cx - width * 0.25f, cy - height * 0.3f, cx - width * 0.1f, cy - height * 0.2f, cx, cy)
        path.close()

        // Right External Petal (Drooping low)
        path.moveTo(cx, cy)
        path.cubicTo(cx + width * 0.35f, cy, cx + width * 0.45f, cy - height * 0.2f, cx + width * 0.38f, cy - height * 0.35f)
        path.cubicTo(cx + width * 0.25f, cy - height * 0.3f, cx + width * 0.1f, cy - height * 0.2f, cx, cy)
        path.close()

        // Central Ornamental Drop hanging down
        path.moveTo(cx, cy)
        path.lineTo(cx, cy + height * 0.15f)
        path.moveTo(cx, cy + height * 0.2f)
        path.addOval(Rect(cx - 3f, cy + height * 0.22f, cx + 3f, cy + height * 0.22f + 6f))

        return path
    }

    fun getMoonPath(width: Float, height: Float): Path {
        val path = Path()
        val cx = width / 2f
        val cy = height / 2f
        val r = Math.min(width, height) / 2.5f

        // Outer Arc of Crescent Moon
        path.moveTo(cx - r * 0.3f, cy - r)
        path.cubicTo(cx + r * 0.7f, cy - r, cx + r, cy - r * 0.5f, cx + r, cy)
        path.cubicTo(cx + r, cy + r * 0.5f, cx + r * 0.7f, cy + r, cx - r * 0.3f, cy + r)
        
        // Inner cutting Arc creating Crescent
        path.cubicTo(cx + r * 0.2f, cy + r, cx + r * 0.45f, cy + r * 0.4f, cx + r * 0.45f, cy)
        path.cubicTo(cx + r * 0.45f, cy - r * 0.4f, cx + r * 0.2f, cy - r, cx - r * 0.3f, cy - r)
        path.close()

        // Small 4-pointed star inside the crescent opening
        val sx = cx - r * 0.1f
        val sy = cy
        val sw = r * 0.2f
        
        path.moveTo(sx, sy - sw)
        path.quadraticTo(sx, sy, sx + sw, sy)
        path.quadraticTo(sx, sy, sx, sy + sw)
        path.quadraticTo(sx, sy, sx - sw, sy)
        path.quadraticTo(sx, sy, sx, sy - sw)
        path.close()

        return path
    }

    fun getButterflyPath(width: Float, height: Float): Path {
        val path = Path()
        val cx = width / 2f
        val cy = height / 2f
        
        // Antennas
        path.moveTo(cx, cy - height * 0.2f)
        path.quadraticTo(cx - width * 0.1f, cy - height * 0.4f, cx - width * 0.12f, cy - height * 0.45f)
        path.moveTo(cx, cy - height * 0.2f)
        path.quadraticTo(cx + width * 0.1f, cy - height * 0.4f, cx + width * 0.12f, cy - height * 0.45f)

        // Symmetric Left Top Wing
        path.moveTo(cx, cy - height * 0.05f)
        path.cubicTo(cx - width * 0.25f, cy - height * 0.45f, cx - width * 0.5f, cy - height * 0.3f, cx - width * 0.45f, cy - height * 0.05f)
        path.cubicTo(cx - width * 0.35f, cy + height * 0.1f, cx - width * 0.15f, cy, cx, cy + height * 0.05f)
        path.close()

        // Symmetric Right Top Wing
        path.moveTo(cx, cy - height * 0.05f)
        path.cubicTo(cx + width * 0.25f, cy - height * 0.45f, cx + width * 0.5f, cy - height * 0.3f, cx + width * 0.45f, cy - height * 0.05f)
        path.cubicTo(cx + width * 0.35f, cy + height * 0.1f, cx + width * 0.15f, cy, cx, cy + height * 0.05f)
        path.close()

        // Symmetric Left Bottom Wing
        path.moveTo(cx, cy + height * 0.05f)
        path.cubicTo(cx - width * 0.2f, cy, cx - width * 0.35f, cy + height * 0.1f, cx - width * 0.3f, cy + height * 0.3f)
        path.cubicTo(cx - width * 0.2f, cy + height * 0.4f, cx - width * 0.05f, cy + height * 0.25f, cx, cy + height * 0.15f)
        path.close()

        // Symmetric Right Bottom Wing
        path.moveTo(cx, cy + height * 0.05f)
        path.cubicTo(cx + width * 0.2f, cy, cx + width * 0.35f, cy + height * 0.1f, cx + width * 0.3f, cy + height * 0.3f)
        path.cubicTo(cx + width * 0.2f, cy + height * 0.4f, cx + width * 0.05f, cy + height * 0.25f, cx, cy + height * 0.15f)
        path.close()

        // Slim Butterfly Body
        path.addOval(Rect(cx - width * 0.02f, cy - height * 0.22f, cx + width * 0.02f, cy + height * 0.22f))

        return path
    }

    fun getOrnamentoGeometricoPath(width: Float, height: Float): Path {
        val path = Path()
        val cx = width / 2f
        val cy = height / 2f
        val size = Math.min(width, height) * 0.8f
        val s = size / 2f

        // Outer Diamond (Rotated Square)
        path.moveTo(cx, cy - s)
        path.lineTo(cx + s, cy)
        path.lineTo(cx, cy + s)
        path.lineTo(cx - s, cy)
        path.close()

        // Inner Concentric Diamond
        path.moveTo(cx, cy - s * 0.7f)
        path.lineTo(cx + s * 0.7f, cy)
        path.lineTo(cx, cy + s * 0.7f)
        path.lineTo(cx - s * 0.7f, cy)
        path.close()

        // Inner circular target
        path.addOval(Rect(cx - s * 0.35f, cy - s * 0.35f, cx + s * 0.35f, cy + s * 0.35f))

        // Center dot
        path.addOval(Rect(cx - 3f, cy - 3f, cx + 3f, cy + 3f))

        // Hanging details and arrows (fine ornamental lines)
        path.moveTo(cx, cy - s)
        path.lineTo(cx, cy - s * 1.2f)
        path.moveTo(cx, cy + s)
        path.lineTo(cx, cy + s * 1.2f)
        
        path.moveTo(cx - s, cy)
        path.lineTo(cx - s * 1.2f, cy)
        path.moveTo(cx + s, cy)
        path.lineTo(cx + s * 1.2f, cy)

        // Hanging ornamental dots at the bottom
        path.addOval(Rect(cx - 4f, cy + s * 1.3f, cx + 4f, cy + s * 1.3f + 8f))
        path.addOval(Rect(cx - 3f, cy + s * 1.45f, cx + 3f, cy + s * 1.45f + 6f))

        return path
    }
}
