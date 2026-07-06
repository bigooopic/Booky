package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Book
import com.example.data.BookPage
import com.example.data.BookPageConverter
import com.example.data.BookRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

// --- ENUMS & SEALS ---
enum class AppTheme { LIGHT, DARK, DARK_GREEN, DARK_BLUE }
enum class Language { FA, EN }
enum class FontType { SANS_SERIF, SERIF, MONOSPACE }

sealed class Screen {
    object BookList : Screen()
    data class Reader(val book: Book) : Screen()
    data class Creator(val book: Book?) : Screen()
}

data class Line(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float = 8f
)

// --- RECONSTRUCT COLOR TO ARGB FOR BITMAP DRAWING ---
fun Color.toArgbInt(): Int {
    return (this.alpha * 255).toInt() shl 24 or
            ((this.red * 255).toInt() shl 16) or
            ((this.green * 255).toInt() shl 8) or
            (this.blue * 255).toInt()
}

// --- CANVAS LINES TO BITMAP UTILITIES ---
fun linesToBitmap(lines: List<Line>, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    }

    for (line in lines) {
        if (line.points.size < 2) continue
        paint.color = line.color.toArgbInt()
        paint.strokeWidth = line.strokeWidth
        val path = android.graphics.Path()
        path.moveTo(line.points[0].x, line.points[0].y)
        for (i in 1 until line.points.size) {
            path.lineTo(line.points[i].x, line.points[i].y)
        }
        canvas.drawPath(path, paint)
    }
    return bitmap
}

fun encodeBitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun base64ToImageBitmap(base64Str: String): ImageBitmap? {
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

// --- PROGRAMMATIC GEOMETRIC PRESETS GENERATOR ---
fun generatePresetBitmap(presetIndex: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.parseColor("#FDFBF7"))

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = android.graphics.Paint.Cap.ROUND
    }

    val cx = 200f
    val cy = 200f

    when (presetIndex) {
        0 -> { // Mystic Star
            paint.color = android.graphics.Color.parseColor("#D32F2F")
            canvas.drawCircle(cx, cy, 140f, paint)
            paint.color = android.graphics.Color.parseColor("#FBC02D")
            val path = android.graphics.Path()
            val points = listOf(
                Pair(cx, cy - 100f),
                Pair(cx + 30f, cy - 30f),
                Pair(cx + 100f, cy - 30f),
                Pair(cx + 45f, cy + 15f),
                Pair(cx + 70f, cy + 90f),
                Pair(cx, cy + 50f),
                Pair(cx - 70f, cy + 90f),
                Pair(cx - 45f, cy + 15f),
                Pair(cx - 100f, cy - 30f),
                Pair(cx - 30f, cy - 30f)
            )
            path.moveTo(points[0].first, points[0].second)
            for (i in 1 until points.size) {
                path.lineTo(points[i].first, points[i].second)
            }
            path.close()
            canvas.drawPath(path, paint)
        }
        1 -> { // Blooming Rose / Mandala
            paint.color = android.graphics.Color.parseColor("#D32F2F")
            val radius = 70f
            canvas.drawCircle(cx, cy, radius, paint)
            paint.color = android.graphics.Color.parseColor("#2E7D32")
            for (angle in 0 until 360 step 60) {
                val rad = Math.toRadians(angle.toDouble())
                val x = cx + radius * Math.cos(rad).toFloat()
                val y = cy + radius * Math.sin(rad).toFloat()
                canvas.drawCircle(x, y, radius, paint)
            }
        }
        2 -> { // Deep Heart
            paint.color = android.graphics.Color.parseColor("#D32F2F")
            val path = android.graphics.Path()
            path.moveTo(cx, cy - 50f)
            path.cubicTo(cx - 70f, cy - 120f, cx - 140f, cy - 50f, cx, cy + 100f)
            path.cubicTo(cx + 140f, cy - 50f, cx + 70f, cy - 120f, cx, cy - 50f)
            canvas.drawPath(path, paint)

            paint.color = android.graphics.Color.parseColor("#FBC02D")
            paint.strokeWidth = 6f
            val innerPath = android.graphics.Path()
            innerPath.moveTo(cx, cy - 20f)
            innerPath.cubicTo(cx - 40f, cy - 70f, cx - 80f, cy - 20f, cx, cy + 70f)
            innerPath.cubicTo(cx + 80f, cy - 20f, cx + 40f, cy - 70f, cx, cy - 20f)
            canvas.drawPath(innerPath, paint)
        }
        3 -> { // Sacred Flower
            paint.color = android.graphics.Color.parseColor("#1565C0")
            canvas.drawCircle(cx, cy, 40f, paint)
            paint.color = android.graphics.Color.parseColor("#FBC02D")
            for (angle in 0 until 360 step 45) {
                val rad = Math.toRadians(angle.toDouble())
                val px = cx + 80f * Math.cos(rad).toFloat()
                val py = cy + 80f * Math.sin(rad).toFloat()
                canvas.drawCircle(px, py, 30f, paint)
            }
        }
    }
    return bitmap
}

// --- LOCALIZATION DICTIONARY ---
object Locales {
    val strings = mapOf(
        Language.EN to mapOf(
            "app_title" to "Booky",
            "reader_tab" to "Book Reader",
            "creator_tab" to "Book Creator",
            "add_book" to "Create New Book",
            "edit_book" to "Edit Book",
            "title_label" to "Book Title",
            "author_label" to "Author Name",
            "save" to "Save Book",
            "delete" to "Delete Book",
            "cancel" to "Cancel",
            "add_page" to "Add Page",
            "delete_page" to "Delete Page",
            "page_label" to "Page %d",
            "text_placeholder" to "Write your page story here...",
            "scroll_mode" to "Scroll View",
            "flip_mode" to "Page Flip",
            "font_settings" to "Font & Layout Settings",
            "theme" to "Application Theme",
            "theme_light" to "Light",
            "theme_dark" to "Dark",
            "theme_green" to "Dark Green",
            "theme_blue" to "Dark Blue",
            "language" to "App Language",
            "font_size" to "Text Size",
            "font_size_small" to "Small",
            "font_size_medium" to "Medium",
            "font_size_large" to "Large",
            "font_size_xlarge" to "Extra Large",
            "font_type" to "Font Style",
            "font_sans" to "Modern (Sans)",
            "font_serif" to "Classic (Serif)",
            "font_mono" to "Typewriter (Mono)",
            "export_pdf" to "Export to PDF",
            "pdf_success" to "PDF created successfully!",
            "pdf_error" to "Failed to create PDF",
            "share_pdf" to "Share PDF",
            "no_books" to "No books found. Create one now!",
            "by_author" to "By %s",
            "draw_illustration" to "Draw Page Illustration",
            "clear_canvas" to "Clear Drawing",
            "select_color" to "Pen Color",
            "drawn_image" to "Sketch Attached",
            "empty_canvas_alert" to "Please sketch something first",
            "save_sketch" to "Confirm Sketch",
            "back" to "Back",
            "add_image_gallery" to "Upload Photo",
            "add_preloaded" to "Preloaded Art",
            "canvas_drawing" to "Hand-draw Sketch",
            "illustration" to "Illustration",
            "no_image" to "No Illustration",
            "select_preset_art" to "Select Preloaded Art",
            "confirm" to "Confirm"
        ),
        Language.FA to mapOf(
            "app_title" to "بوکی (Booky)",
            "reader_tab" to "کتاب خوان",
            "creator_tab" to "کتاب ساز",
            "add_book" to "ساخت کتاب جدید",
            "edit_book" to "ویرایش کتاب",
            "title_label" to "عنوان کتاب",
            "author_label" to "نام نویسنده",
            "save" to "ذخیره کتاب",
            "delete" to "حذف کتاب",
            "cancel" to "انصراف",
            "add_page" to "افزودن صفحه",
            "delete_page" to "حذف صفحه",
            "page_label" to "صفحه %d",
            "text_placeholder" to "داستان صفحه خود را اینجا بنویسید...",
            "scroll_mode" to "نمایش طوماری (Scroll)",
            "flip_mode" to "ورق زدن صفحه (Flip)",
            "font_settings" to "تنظیمات قلم و متن",
            "theme" to "تم برنامه",
            "theme_light" to "روشن",
            "theme_dark" to "تیره",
            "theme_green" to "سبز تیره",
            "theme_blue" to "آبی تیره",
            "language" to "زبان برنامه",
            "font_size" to "اندازه متن",
            "font_size_small" to "کوچک",
            "font_size_medium" to "متوسط",
            "font_size_large" to "بزرگ",
            "font_size_xlarge" to "خیلی بزرگ",
            "font_type" to "نوع فونت",
            "font_sans" to "مدرن (Sans)",
            "font_serif" to "کتابی (Serif)",
            "font_mono" to "ماشین تحریر (Mono)",
            "export_pdf" to "خروجی PDF",
            "pdf_success" to "فایل PDF با موفقیت ساخته شد!",
            "pdf_error" to "خطا در ساخت PDF",
            "share_pdf" to "اشتراک‌گذاری PDF",
            "no_books" to "کتابی یافت نشد. یک کتاب جدید بسازید!",
            "by_author" to "اثری از %s",
            "draw_illustration" to "ترسیم نقاشی برای صفحه",
            "clear_canvas" to "پاک کردن صفحه",
            "select_color" to "رنگ قلم",
            "drawn_image" to "تصویر نقاشی ثبت شد",
            "empty_canvas_alert" to "لطفا ابتدا نقاشی بکشید",
            "save_sketch" to "ثبت نقاشی",
            "back" to "بازگشت",
            "add_image_gallery" to "انتخاب عکس",
            "add_preloaded" to "طرح‌های آماده",
            "canvas_drawing" to "کشیدن نقاشی",
            "illustration" to "تصویر صفحه",
            "no_image" to "بدون تصویر",
            "select_preset_art" to "انتخاب طرح هنری آماده",
            "confirm" to "تایید"
        )
    )

    fun getString(lang: Language, key: String, vararg args: Any): String {
        val base = strings[lang]?.get(key) ?: strings[Language.EN]?.get(key) ?: key
        return try {
            String.format(base, *args)
        } catch (e: Exception) {
            base
        }
    }
}

// --- THEME UTILITY ---
@Composable
fun BookyThemeProvider(
    theme: AppTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.LIGHT -> lightColorScheme(
            background = Color(0xFFFDFBF7),
            surface = Color(0xFFF5F2EB),
            onBackground = Color(0xFF1C1C1E),
            onSurface = Color(0xFF1C1C1E),
            primary = Color(0xFFDC2626),
            secondary = Color(0xFFEAB308),
            surfaceVariant = Color(0xFFEAE4D8)
        )
        AppTheme.DARK -> darkColorScheme(
            background = Color(0xFF121214),
            surface = Color(0xFF1E1E22),
            onBackground = Color(0xFFE5E5EA),
            onSurface = Color(0xFFE5E5EA),
            primary = Color(0xFFDC2626),
            secondary = Color(0xFFEAB308),
            surfaceVariant = Color(0xFF2B2B30)
        )
        AppTheme.DARK_GREEN -> darkColorScheme(
            background = Color(0xFF0D1F14),
            surface = Color(0xFF172A1E),
            onBackground = Color(0xFFE2EFE4),
            onSurface = Color(0xFFE2EFE4),
            primary = Color(0xFFDC2626),
            secondary = Color(0xFFEAB308),
            surfaceVariant = Color(0xFF21382A)
        )
        AppTheme.DARK_BLUE -> darkColorScheme(
            background = Color(0xFF0B1222),
            surface = Color(0xFF181E2D),
            onBackground = Color(0xFFF1F5F9),
            onSurface = Color(0xFFF1F5F9),
            primary = Color(0xFFDC2626),
            secondary = Color(0xFFEAB308),
            surfaceVariant = Color(0xFF232A3C)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// --- PERSISTENT STATE VIEWMODEL ---
class BookyViewModel(private val repository: BookRepository, context: Context) : ViewModel() {
    private val prefs = context.getSharedPreferences("booky_prefs", Context.MODE_PRIVATE)

    var currentTheme by mutableStateOf(AppTheme.valueOf(prefs.getString("theme", AppTheme.DARK_BLUE.name) ?: AppTheme.DARK_BLUE.name))
        private set

    var currentLanguage by mutableStateOf(Language.valueOf(prefs.getString("lang", Language.FA.name) ?: Language.FA.name))
        private set

    var fontSize by mutableStateOf(prefs.getInt("font_size", 18))
        private set

    var fontType by mutableStateOf(FontType.valueOf(prefs.getString("font_type", FontType.SERIF.name) ?: FontType.SERIF.name))
        private set

    var currentScreen by mutableStateOf<Screen>(Screen.BookList)

    var readerMode by mutableStateOf(prefs.getString("reader_mode", "FLIP") ?: "FLIP")

    var creatorModeActive by mutableStateOf(false)

    val booksState = repository.allBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.ensureSampleBooks()
        }
    }

    fun setTheme(theme: AppTheme) {
        currentTheme = theme
        prefs.edit().putString("theme", theme.name).apply()
    }

    fun setLanguage(lang: Language) {
        currentLanguage = lang
        prefs.edit().putString("lang", lang.name).apply()
    }

    fun setFontSizeValue(size: Int) {
        fontSize = size
        prefs.edit().putInt("font_size", size).apply()
    }

    fun setFontTypeValue(type: FontType) {
        fontType = type
        prefs.edit().putString("font_type", type.name).apply()
    }

    fun setReaderModeValue(mode: String) {
        readerMode = mode
        prefs.edit().putString("reader_mode", mode).apply()
    }

    fun saveBook(book: Book) {
        viewModelScope.launch {
            if (book.id == 0L) {
                repository.insertBook(book)
            } else {
                repository.updateBook(book)
            }
            currentScreen = Screen.BookList
        }
    }

    fun deleteBook(id: Long) {
        viewModelScope.launch {
            repository.deleteBookById(id)
        }
    }
}

// --- STANDARD REUSABLE APP BUTTONS ---
@Composable
fun BookyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true, // true: Red, false: Yellow
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp), // Elegant rounded-2xl
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) Color(0xFFDC2626) else Color(0xFFEAB308),
            contentColor = if (isPrimary) Color.White else Color(0xFF0B1222)
        ),
        modifier = modifier
            .height(48.dp)
            .testTag(if (isPrimary) "btn_primary_$text" else "btn_secondary_$text")
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isPrimary) Color.White else Color(0xFF0B1222)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

// --- DIALOGS FOR CREATOR ILLUSTRATION ---
@Composable
fun DrawingCanvasDialog(
    lang: Language,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var lines = remember { mutableStateListOf<Line>() }
    var currentLinePoints = remember { mutableStateListOf<Offset>() }
    var selectedColor by remember { mutableStateOf(Color.Black) }

    val colors = listOf(
        Color.Black,
        Color(0xFFD32F2F),
        Color(0xFFFBC02D),
        Color(0xFF2E7D32),
        Color(0xFF1565C0)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            BookyButton(
                text = Locales.getString(lang, "save_sketch"),
                isPrimary = true,
                onClick = {
                    if (lines.isEmpty()) {
                        // Skip empty
                    } else {
                        val bitmap = linesToBitmap(lines.toList(), 400, 400)
                        val base64 = encodeBitmapToBase64(bitmap)
                        onSave(base64)
                    }
                }
            )
        },
        dismissButton = {
            BookyButton(
                text = Locales.getString(lang, "cancel"),
                isPrimary = false,
                onClick = onDismiss
            )
        },
        title = {
            Text(
                text = Locales.getString(lang, "draw_illustration"),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == color) 3.dp else 1.dp,
                                    color = if (selectedColor == color) Color.White else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .border(1.5.dp, Color.Gray, RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentLinePoints.clear()
                                    currentLinePoints.add(offset)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentLinePoints.add(change.position)
                                },
                                onDragEnd = {
                                    if (currentLinePoints.isNotEmpty()) {
                                        lines.add(Line(currentLinePoints.toList(), selectedColor))
                                        currentLinePoints.clear()
                                    }
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        for (line in lines) {
                            if (line.points.size < 2) continue
                            val path = Path()
                            path.moveTo(line.points[0].x, line.points[0].y)
                            for (i in 1 until line.points.size) {
                                path.lineTo(line.points[i].x, line.points[i].y)
                            }
                            drawPath(
                                path = path,
                                color = line.color,
                                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                        if (currentLinePoints.size >= 2) {
                            val path = Path()
                            path.moveTo(currentLinePoints[0].x, currentLinePoints[0].y)
                            for (i in 1 until currentLinePoints.size) {
                                path.lineTo(currentLinePoints[i].x, currentLinePoints[i].y)
                            }
                            drawPath(
                                path = path,
                                color = selectedColor,
                                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                BookyButton(
                    text = Locales.getString(lang, "clear_canvas"),
                    isPrimary = false,
                    onClick = { lines.clear() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun PresetArtDialog(
    lang: Language,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val presetLabels = listOf("Mystic Star", "Blooming Rose", "Deep Heart", "Sacred Flower")
    val presetIcons = listOf(
        Icons.Default.Star,
        Icons.Default.Spa,
        Icons.Default.Favorite,
        Icons.Default.LocalFlorist
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            BookyButton(
                text = Locales.getString(lang, "cancel"),
                isPrimary = false,
                onClick = onDismiss
            )
        },
        title = {
            Text(
                text = Locales.getString(lang, "select_preset_art"),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                presetLabels.forEachIndexed { idx, label ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                val bitmap = generatePresetBitmap(idx)
                                val b64 = encodeBitmapToBase64(bitmap)
                                onSelect(b64)
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = presetIcons[idx],
                            contentDescription = label,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = label,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    )
}

// --- MAIN WRAPPER COMPOSABLE ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookyApp(viewModel: BookyViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentTheme = viewModel.currentTheme
    val lang = viewModel.currentLanguage
    val fontType = viewModel.fontType
    val fontSize = viewModel.fontSize
    val currentScreen = viewModel.currentScreen

    val books by viewModel.booksState.collectAsState()

    val textFontFamily = when (fontType) {
        FontType.SANS_SERIF -> FontFamily.SansSerif
        FontType.SERIF -> FontFamily.Serif
        FontType.MONOSPACE -> FontFamily.Monospace
    }

    BookyThemeProvider(theme = currentTheme) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    val drawerDirection = if (lang == Language.FA) LayoutDirection.Rtl else LayoutDirection.Ltr
                    CompositionLocalProvider(LocalLayoutDirection provides drawerDirection) {
                        ModalDrawerSheet(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(300.dp)
                                .background(MaterialTheme.colorScheme.surface),
                            drawerContainerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = Locales.getString(lang, "font_settings"),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f))

                                Text(
                                    text = Locales.getString(lang, "language"),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    BookyButton(
                                        text = "فارسی",
                                        isPrimary = lang == Language.FA,
                                        onClick = { viewModel.setLanguage(Language.FA) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    BookyButton(
                                        text = "English",
                                        isPrimary = lang == Language.EN,
                                        onClick = { viewModel.setLanguage(Language.EN) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Text(
                                    text = Locales.getString(lang, "font_size"),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val sizes = listOf(14, 18, 22, 28)
                                    val sizeLabels = listOf("font_size_small", "font_size_medium", "font_size_large", "font_size_xlarge")
                                    sizes.forEachIndexed { idx, sz ->
                                        BookyButton(
                                            text = Locales.getString(lang, sizeLabels[idx]),
                                            isPrimary = fontSize == sz,
                                            onClick = { viewModel.setFontSizeValue(sz) }
                                        )
                                    }
                                }

                                Text(
                                    text = Locales.getString(lang, "font_type"),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val types = listOf(FontType.SANS_SERIF, FontType.SERIF, FontType.MONOSPACE)
                                    val typeLabels = listOf("font_sans", "font_serif", "font_mono")
                                    types.forEachIndexed { idx, type ->
                                        BookyButton(
                                            text = Locales.getString(lang, typeLabels[idx]),
                                            isPrimary = fontType == type,
                                            onClick = { viewModel.setFontTypeValue(type) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Text(
                                    text = Locales.getString(lang, "theme"),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val themes = listOf(AppTheme.LIGHT, AppTheme.DARK, AppTheme.DARK_GREEN, AppTheme.DARK_BLUE)
                                    val themeLabels = listOf("theme_light", "theme_dark", "theme_green", "theme_blue")
                                    themes.forEachIndexed { idx, thm ->
                                        BookyButton(
                                            text = Locales.getString(lang, themeLabels[idx]),
                                            isPrimary = currentTheme == thm,
                                            onClick = { viewModel.setTheme(thm) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            ) {
                val mainDirection = if (lang == Language.FA) LayoutDirection.Rtl else LayoutDirection.Ltr
                CompositionLocalProvider(LocalLayoutDirection provides mainDirection) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .windowInsetsPadding(WindowInsets.statusBars)
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (currentScreen !is Screen.BookList) {
                                            IconButton(
                                                onClick = { viewModel.currentScreen = Screen.BookList }
                                            ) {
                                                Icon(
                                                    imageVector = if (lang == Language.FA) Icons.Default.ArrowForward else Icons.Default.ArrowBack,
                                                    contentDescription = Locales.getString(lang, "back"),
                                                    tint = MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.secondary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "B",
                                                    color = Color(0xFF0B1222),
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                        }

                                        Text(
                                            text = when (currentScreen) {
                                                is Screen.BookList -> Locales.getString(lang, "app_title")
                                                is Screen.Reader -> currentScreen.book.title
                                                is Screen.Creator -> Locales.getString(
                                                    lang,
                                                    if (currentScreen.book == null) "add_book" else "edit_book"
                                                )
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            fontFamily = textFontFamily,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                            }
                                        },
                                        modifier = Modifier.testTag("hamburger_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Menu",
                                            tint = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.10f),
                                    thickness = 1.dp
                                )
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "screen_transition"
                            ) { target ->
                                when (target) {
                                    is Screen.BookList -> BookListScreen(
                                        books = books,
                                        lang = lang,
                                        viewModel = viewModel,
                                        onRead = { book -> viewModel.currentScreen = Screen.Reader(book) },
                                        onEdit = { book -> viewModel.currentScreen = Screen.Creator(book) }
                                    )
                                    is Screen.Reader -> ReaderScreen(
                                        book = target.book,
                                        lang = lang,
                                        fontSize = fontSize,
                                        fontFamily = textFontFamily,
                                        viewModel = viewModel
                                    )
                                    is Screen.Creator -> CreatorScreen(
                                        book = target.book,
                                        lang = lang,
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- BOOK LISTING SCREEN ---
@Composable
fun BookListScreen(
    books: List<Book>,
    lang: Language,
    viewModel: BookyViewModel,
    onRead: (Book) -> Unit,
    onEdit: (Book) -> Unit
) {
    val creatorMode = viewModel.creatorModeActive

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BookyButton(
                text = Locales.getString(lang, "reader_tab"),
                isPrimary = !creatorMode,
                onClick = { viewModel.creatorModeActive = false },
                modifier = Modifier.weight(1f)
            )
            BookyButton(
                text = Locales.getString(lang, "creator_tab"),
                isPrimary = creatorMode,
                onClick = { viewModel.creatorModeActive = true },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (books.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Locales.getString(lang, "no_books"),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(books) { _, book ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.10f), RoundedCornerShape(24.dp))
                            .clickable {
                                if (creatorMode) onEdit(book) else onRead(book)
                            }
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFD32F2F).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = book.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = Locales.getString(lang, "by_author", book.author),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                        }

                        if (creatorMode) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { onEdit(book) }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color(0xFFFBC02D)
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteBook(book.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFD32F2F)
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = if (lang == Language.FA) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                                contentDescription = "Read",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        if (creatorMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = { viewModel.currentScreen = Screen.Creator(null) },
                    containerColor = Color(0xFFFBC02D),
                    contentColor = Color(0xFF1C1C1E),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("add_book_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Book", modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

// --- BOOK READER SCREEN ---
@Composable
fun ReaderScreen(
    book: Book,
    lang: Language,
    fontSize: Int,
    fontFamily: FontFamily,
    viewModel: BookyViewModel
) {
    val context = LocalContext.current
    val pages = remember(book) { BookPageConverter.jsonToPages(book.pagesJson) }
    val readerMode = viewModel.readerMode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BookyButton(
                text = Locales.getString(lang, "flip_mode"),
                isPrimary = readerMode == "FLIP",
                onClick = { viewModel.setReaderModeValue("FLIP") },
                modifier = Modifier.weight(1f)
            )
            BookyButton(
                text = Locales.getString(lang, "scroll_mode"),
                isPrimary = readerMode == "SCROLL",
                onClick = { viewModel.setReaderModeValue("SCROLL") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (pages.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Empty Book pages.")
            }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                if (readerMode == "FLIP") {
                    val pagerState = rememberPagerState(pageCount = { pages.size })
                    val scope = rememberCoroutineScope()

                    Column(modifier = Modifier.fillMaxSize()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) { pageIdx ->
                            val page = pages[pageIdx]
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.10f), RoundedCornerShape(24.dp))
                                    .verticalScroll(rememberScrollState())
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (!page.imageData.isNullOrEmpty()) {
                                    val bitmap = base64ToImageBitmap(page.imageData)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = "Page Art",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }

                                Text(
                                    text = page.text,
                                    fontSize = fontSize.sp,
                                    fontFamily = fontFamily,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    lineHeight = (fontSize * 1.5).sp,
                                    textAlign = if (lang == Language.FA) TextAlign.Right else TextAlign.Left,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BookyButton(
                                text = "<",
                                isPrimary = false,
                                onClick = {
                                    scope.launch {
                                        if (pagerState.currentPage > 0) {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    }
                                },
                                enabled = pagerState.currentPage > 0
                            )

                            Text(
                                text = "${pagerState.currentPage + 1} / ${pages.size}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            BookyButton(
                                text = ">",
                                isPrimary = false,
                                onClick = {
                                    scope.launch {
                                        if (pagerState.currentPage < pages.size - 1) {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    }
                                },
                                enabled = pagerState.currentPage < pages.size - 1
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(pages) { idx, page ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.10f), RoundedCornerShape(24.dp))
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = Locales.getString(lang, "page_label", idx + 1),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                if (!page.imageData.isNullOrEmpty()) {
                                    val bitmap = base64ToImageBitmap(page.imageData)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = "Page illustration",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }

                                Text(
                                    text = page.text,
                                    fontSize = fontSize.sp,
                                    fontFamily = fontFamily,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    lineHeight = (fontSize * 1.5).sp,
                                    textAlign = if (lang == Language.FA) TextAlign.Right else TextAlign.Left,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BookyButton(
            text = Locales.getString(lang, "export_pdf"),
            isPrimary = true,
            icon = Icons.Default.PictureAsPdf,
            onClick = {
                val pdfFile = PdfExporter.exportBookToPdf(context, book, fontSize)
                if (pdfFile != null && pdfFile.exists()) {
                    Toast.makeText(context, Locales.getString(lang, "pdf_success"), Toast.LENGTH_LONG).show()

                    try {
                        val fileUri = FileProvider.getUriForFile(
                            context,
                            "com.example.booky.fileprovider",
                            pdfFile
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, Locales.getString(lang, "share_pdf")))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(context, Locales.getString(lang, "pdf_error"), Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// --- BOOK CREATOR / EDITOR SCREEN ---
@Composable
fun CreatorScreen(
    book: Book?,
    lang: Language,
    viewModel: BookyViewModel
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(book?.title ?: "") }
    var author by remember { mutableStateOf(book?.author ?: "") }

    val initialPages = remember(book) {
        if (book != null) BookPageConverter.jsonToPages(book.pagesJson).toMutableStateList()
        else mutableStateListOf(BookPage(1, ""))
    }
    val pagesList = remember { initialPages }

    var showDrawingCanvasIdx by remember { mutableStateOf<Int?>(null) }
    var showPresetArtIdx by remember { mutableStateOf<Int?>(null) }

    // State variable declared BEFORE the launcher which depends on it
    var awaitingImageUploadIdx by remember { mutableStateOf<Int?>(null) }

    // Gallery Photo Loader
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val base64 = encodeBitmapToBase64(bitmap)
                    val idx = awaitingImageUploadIdx
                    if (idx != null && idx in pagesList.indices) {
                        val page = pagesList[idx]
                        pagesList[idx] = page.copy(imageData = base64)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.10f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(Locales.getString(lang, "title_label")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_book_title"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text(Locales.getString(lang, "author_label")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_book_author"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        itemsIndexed(pagesList) { pageIdx, page ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.10f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Locales.getString(lang, "page_label", pageIdx + 1),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (pagesList.size > 1) {
                            IconButton(onClick = { pagesList.removeAt(pageIdx) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = Locales.getString(lang, "delete_page"),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = page.text,
                        onValueChange = { txt ->
                            pagesList[pageIdx] = page.copy(text = txt)
                        },
                        placeholder = { Text(Locales.getString(lang, "text_placeholder")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    if (!page.imageData.isNullOrEmpty()) {
                        val bitmap = base64ToImageBitmap(page.imageData)
                        if (bitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                            ) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Illustration preview",
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { pagesList[pageIdx] = page.copy(imageData = null) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Art",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = Locales.getString(lang, "no_image"),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BookyButton(
                            text = Locales.getString(lang, "canvas_drawing"),
                            isPrimary = false,
                            onClick = { showDrawingCanvasIdx = pageIdx },
                            modifier = Modifier.weight(1f)
                        )

                        BookyButton(
                            text = Locales.getString(lang, "add_preloaded"),
                            isPrimary = false,
                            onClick = { showPresetArtIdx = pageIdx },
                            modifier = Modifier.weight(1f)
                        )

                        BookyButton(
                            text = Locales.getString(lang, "add_image_gallery"),
                            isPrimary = false,
                            onClick = {
                                awaitingImageUploadIdx = pageIdx
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            BookyButton(
                text = Locales.getString(lang, "add_page"),
                isPrimary = false,
                icon = Icons.Default.Add,
                onClick = {
                    pagesList.add(BookPage(pagesList.size + 1, ""))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BookyButton(
                    text = Locales.getString(lang, "cancel"),
                    isPrimary = false,
                    onClick = { viewModel.currentScreen = Screen.BookList },
                    modifier = Modifier.weight(1f)
                )

                BookyButton(
                    text = Locales.getString(lang, "save"),
                    isPrimary = true,
                    onClick = {
                        if (title.isNotEmpty()) {
                            val finalBook = Book(
                                id = book?.id ?: 0L,
                                title = title,
                                author = author.ifEmpty { "Anonymous" },
                                pagesJson = BookPageConverter.pagesToJson(pagesList.toList())
                            )
                            viewModel.saveBook(finalBook)
                        } else {
                            Toast.makeText(context, "Title required", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }

    if (showDrawingCanvasIdx != null) {
        val pageIdx = showDrawingCanvasIdx!!
        DrawingCanvasDialog(
            lang = lang,
            onDismiss = { showDrawingCanvasIdx = null },
            onSave = { b64 ->
                val page = pagesList[pageIdx]
                pagesList[pageIdx] = page.copy(imageData = b64)
                showDrawingCanvasIdx = null
            }
        )
    }

    if (showPresetArtIdx != null) {
        val pageIdx = showPresetArtIdx!!
        PresetArtDialog(
            lang = lang,
            onDismiss = { showPresetArtIdx = null },
            onSelect = { b64 ->
                val page = pagesList[pageIdx]
                pagesList[pageIdx] = page.copy(imageData = b64)
                showPresetArtIdx = null
            }
        )
    }
}
