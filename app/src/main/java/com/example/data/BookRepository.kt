package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class BookRepository(private val context: Context) {
    private val database: BookDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            BookDatabase::class.java,
            "booky_database"
        ).build()
    }

    private val bookDao = database.bookDao()

    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()

    suspend fun getBookById(id: Long): Book? {
        return bookDao.getBookById(id)
    }

    suspend fun insertBook(book: Book): Long {
        return bookDao.insertBook(book)
    }

    suspend fun updateBook(book: Book) {
        bookDao.updateBook(book)
    }

    suspend fun deleteBookById(id: Long) {
        bookDao.deleteBookById(id)
    }

    suspend fun ensureSampleBooks() {
        val books = allBooks.map { it }.first()
        if (books.isEmpty()) {
            // Preload 2 books
            val samplePages1 = listOf(
                BookPage(
                    1,
                    "شازده کوچولو پرسید: با وفاترین موجود دنیا کیست؟ روباه گفت: سگی که بفهمد صاحبش هیچ دارایی ندارد ولی باز هم مثل یک سلطان با او رفتار می کند و او را ترک نمی کند...",
                    SAMPLE_STAR_IMAGE
                ),
                BookPage(
                    2,
                    "روباه گفت: انسان‌ها این حقیقت را فراموش کرده‌اند اما تو نباید فراموشش کنی. تو تا زنده‌ای نسبت به آنی که اهلی کرده‌ای مسئولی. تو مسئول گلتی...",
                    SAMPLE_ROSE_IMAGE
                )
            )
            val book1 = Book(
                title = "شازده کوچولو (نمونه)",
                author = "آنتوان دو سنت اگزوپری",
                pagesJson = BookPageConverter.pagesToJson(samplePages1)
            )
            insertBook(book1)

            val samplePages2 = listOf(
                BookPage(
                    1,
                    "ای برادر تو همان اندیشه‌ای\nمابقی تو استخوان و ریشه‌ای\n\nگر گلست اندیشه‌ تو گلشنی\nور بود خاری تو هیمه گلخنی",
                    SAMPLE_HEART_IMAGE
                ),
                BookPage(
                    2,
                    "عشق آموخت مرا شکل دگر خندیدن\nدلم از چرخ برون رفت به چرخیدن...\n\nدیده سیر است مرا جان دلیر است مرا\nزهره شیر است مرا تا به اسد همبر شدم",
                    SAMPLE_FLOWER_IMAGE
                )
            )
            val book2 = Book(
                title = "حکمت‌های مولانا (نمونه)",
                author = "مولوی",
                pagesJson = BookPageConverter.pagesToJson(samplePages2)
            )
            insertBook(book2)

            val samplePages3 = listOf(
                BookPage(
                    1,
                    "Welcome to Booky! This is your custom book creator and reader. You can read, create, edit, or delete books. Use the Hamburger menu on the top right to customize themes, languages, and fonts.",
                    null
                ),
                BookPage(
                    2,
                    "To create a new book, tap the yellow '+' button on the home page. You can add text, draw with a stylus/finger on the canvas, or select an image from your device! Then save and click 'Export to PDF' to share.",
                    null
                )
            )
            val book3 = Book(
                title = "Welcome to Booky!",
                author = "Booky Team",
                pagesJson = BookPageConverter.pagesToJson(samplePages3)
            )
            insertBook(book3)
        }
    }

    companion object {
        // Simple base64 values representing tiny, minimalist solid/outline visual drawings
        // to make preloaded books look stunning without needing heavy image files.
        // Star outline image
        private const val SAMPLE_STAR_IMAGE = 
            "iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAYAAABw4pVUAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH" +
            "6AcDExEBFfOfCgAABp5JREFUeNrtm3uIHEUUxr9qb9O7O/HYZHexWWMWNREfKIKCiiKi+EBTg0gEBf9IkIigYEA0gg+CoCAiPhAUEUVERDQi" +
            "iH8UFUQU9QEoRveuyW7isTvZ3Z1ejz+qq6unp2e6Z3u6Z6unfwyzM9Vd9Vvdde89p06dEgSBIAgEQSAIgv9r9Xo9g6bA8bK6vH5en68OUL9f" +
            "r1/Yf7q9DqitN9XlWfU51v68pM79Xj9fr6/R88E3H9X+Z+3vs0TqgK4D+C+9XUeA59R+Vp/H1eE7Xm0fCUB/jNofreN1PABvF3A8D+pPrX+Y" +
            "B76p69fWb6wfWN/P2t969PZJ7f+p9R+1P679H9dZ389fO6R3yPqfWb8T4On638g+6UAnFvAtXb/D/V5C6g5U+3bW6zD3S8Pcrw3z+mC/d9rv" +
            "O839vs76vV8f0PZ3Tfu0v3vP+uGvKTo6Z73p9S0A61ivK6TujK+/sX6XpMvUv6O/N83rO+t90/qusc4K9/89169x92/V/rW82nps/8b7N/6e" +
            "O2S/t9R3qf1V9XsFfK1G/K1W/P/Y5f4K96v9v6L967R/nXtI0v5V7l9m/Z6uPyH1Z0r9mZ9O2rD/pX7P+p9Tf+bnR0m9XfU9f/eQA97Xf2K/" +
            "Z/3P889WvCscu8Kxx+vXuNfXqf11bZc3Tvvv2Z+T7N8m7l/k9TXuv4bUrtfW79/vDrlQ60/T99f6U9v6Z+jvWe6f4fUL9XeV9fWq+g6pD6vN" +
            "g3bIDfS/8fe8Xl+/F/B67P8vA94X/F6vv6+s71/T7+vXFdf366l/AuuH/B5yp/7u/D1rvUXv99j/n8/r+/V7vL/u8PoVjt3fP6D/+9X/eO3f" +
            "Yv322e/f/z0EAIIgCMyKAnmKz2H8zGbeK67h8/V9+E8fB+fN72pYvD6Pr6/N5/N1vK6WfS1+V9O99fXz9et4fV1P6g5Un8/zM+/T5/V5fW9/" +
            "z/Y3nfdq97V47F7tvhaP3and1+I63v666V677zDuvofY76H2O/g96Pca/N6U/b2g99+S/b1R96+0fUvS/WvsvqXWd8n+vmn3r9G67Xq+ZfuS" +
            "2C7Xm2P7u/pL5N8e4vdN7NdfbL/fH/82p2eK5P+g76nZ3+T1fZZsX/pX2H+p79Hva/u9ObeXf7Hff3Fm/8XW70vY9pXb9qVsh2Vbyvblm+xb" +
            "yvZbyvaVbEum/F6Vf0vZvip2WF6Wj02Z8q8p21eyfam8z3zN/GvK9id976zvkfU9ZbeXt31/bN+feE/Z7SUp+96U/W2S7vskbF+N275q2VbU" +
            "fclb6O5b8rYlu/8W2ffXfNshBIIgCILDeUshPshfJv/C6/g8Pr/F62v5eG0ffZyu8fqaGvY1NfF9jZ6vGfD9Gq5fw6/reF1NHP/6Gl5fw+/r" +
            "uPse4vU9ub7W9v3Z/L62/m79fTY/27L3S7f6Wnr6paf7Xtr3VfT6Xlr3VfT6Hln3/Wnzq6U9XzX6fNXY6/NU/fN69vlqnC+f3n08Ndf7p/v9" +
            "07vOq7E6VunXm3C+VfXv9K6zqu6V3nWq6N+U9m9K9W9KujcleyUpeyUpW0W6V4reN0XvW2nfW6nvrdT3S7r9K9W9K+37krR9SbpXvL4vSddX" +
            "0dfX0NfX0H+m6M8UxX6m8M8UuWeK/B7ie0reXlV9q+X3lNxepXuWzN6S7S3Z3pS9UvYV/u8gEARBYLgGZpX+bMvPL9vy82O5Ppf079v/25b6" +
            "t9vyb0uP9G/f1f8b63V8999U/9f6P9T/Ff7vFf9v/79/K/rvXor0f9fe/w7W/07P/v17698+of7N8epXp8S/OV69epfP7/K8epUen1fpefX8" +
            "Lp/v5X95Xpe//+Z7/T47fE77bA73q3O9PpeXmOsz3ev95fC12uv8Pvt/v8S7zK/R3mXm1+j9U9I/Jf399L/U+39P9N6U8fS2/kU8X5N8f9L2" +
            "J7ntT/L7p+S2Pyk5X1PivSby9qrEeyUpXitKzWvEeyVv9vXg83r+Vof3b67/W7SveXN9VffZpXfN0nvNuIfoXbeKf80O/TXD/Wv8v2Z6f00e" +
            "79dM96/x/zXT+6fF99dM+9eE7Wvkvp/U36/k/X5SPr+Wv6/v9f6++v67fP/9b2N9Fv9nIAgCwX8F/gH4l0GqR/4RBAAAAABJRU5ErkJggg=="

        // Rose flower outline
        private const val SAMPLE_ROSE_IMAGE =
            "iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAYAAABw4pVUAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH" +
            "6AcDExIBrM1vBwAAB65JREFUeNrtm3uMXFUdxz9ntm92Z3e2W7alL6C0pUApgZbyiI8gYAsYpX9IDFEgYgIxf6gYg0b9g6gYNYofgMQHUTRI" +
            "fKAYEowQQZCH0vIolLa0u9vtdmfnvnbm+MfZmTu7O/fOzO7OnbOf5Mvs3Z0799zzu79f53e+53e+Z0REhBBiBNo1P0Xv5HlyT57YfFGe/Dxx" +
            "U97ZPMN5V/N089zm3S27t/muq+0Ltt3N8eZ6bX2779f3C7SrebeL9W2+/7fWf+m19WvrtX7L67m7eabZfG6r7ZfU9mXrX01/E96b6NckN/m2" +
            "77faZ7XaV69/M9831p/p80v9K7X/Z/v0l7ZfUd9XtK9uX6E2f0N9v9b2G/v/qL8ZbeP1tX7N9dfXf2b96vovvLbX8Xb/8b2p/tF90t+U/m30" +
            "r7f/n2mfnGnbqP+96N/O/2XG9C+iPxtt97pYm/K6/B90+W8i2T/of3E/+Z/eT/ZPxv2T7f9Zz4u+vujL2/b/Z9qW/17R9q/r67G267b1lW2f" +
            "Z65/8p006C7fG6/v9vVrebeX622u57m7Wf8Wv9/l776S/n31/dfV/8eW7ZfW6yva/+3uX7Ztv9X9Vf3/atZ9v68vWfN67jS7tW6831mv0a71" +
            "7fXf9F97XvC/f896bY67P6D6mfdLa9on9G/7+YNuUv9b26G0f90m3fZ76pPeF/lptY1u2fXm7pW9t235uP2nb77Uv6NdfVPuP76X6Xun7i/Wv" +
            "s/4Lrf2g9l/v9V/I93m5/u28Xf7+7fbptO3pX699Yr9BbePU17f2iW19fGu/ZNuU/2XGNrX9pPZfby3f6/q+Rdp/m6z9V9of3P6Ltf/2SfsD" +
            "tp/X1ndO+59W+/rWf2D77/baZ619SntfWfvebX1Gex8X2R6X7yvW/lHbP6vtp7T/Gduv07Yx28+3/Xztd46x7eP6X0b/Fvv0u/U3rf/F/hfo" +
            "v9p9pba/Vf9f1/96f63+dfvfb/v1+v962/97beN9+sZpG7vXfrL2v9f+W21f3v5H+wX6/G60Xej9xXm7iPUGtfX6be3/9fX6BtvP6796bX2b" +
            "/uvr32GfWdvG+gX67PZ/W/v/gWz7Ym3/oP3D9rn9g/avpL9ZfeX/4f9pU9v6u8a236P/pfaF/ZrtF3j/e/3v7Xm/0K/z/vfaF/v/V7v2vX7Z" +
            "6u8b+9m2/1b/+9F/qf3Dtp+vbXWrfW7//9K1r2r7atZ+v7f79rWv8Xb7/e+xr/R6nrX9Am37vPYp7dO9/Z9pW//f7Wva/jHtd+uztr/PfvvW" +
            "tH+F/ffuM739vPZptn+99iX91PZ/rS9v7Uv8t/1X2VdpG++96f9Gf+p/+397r+/X9Y3a/1uWffuCbb/Y+96W3Tvavp+2ffp++3T8/Y6/z9m9" +
            "Y+5f0T7F2/0Z3TfWP8P959s+oX2iN/m2T0T/e7p/0vN+TvsZ7me0n9WepO1M95Paj9N+hO7jtB/mfrT2P6I7XfepvjvF/eTaT9Wdlv9B/pPt" +
            "B7mfcD/N/ZibfN/Y/aD7YfbTvNf69nnuR7g/8/3P3A+wH+L+Z7t32P0A9wPt/un9b/H2vXU/6H+uN/n+G7vvcN/BfpA3uG93P8B9G/fN7ofZ" +
            "b/HeYHuz90buh9hvdD/Afj3369mvs6+jv47+S+xr2K/jvob7OveX2f8E9yvZD3C/gN2T/Vvsm9mvdr+K/Uvsvszuy9wvZ/9z9j9tP+h+mfsT" +
            "3L9g9wWeL2f/FfcvWv9Ddp/Xdp6758T+We4Pcz/X3VfsHmbvYfdX3B9k9z67v+f+Hruvsfswd/e6u8vdr9g9xN6b7F5v9/vcfclufvd7uDvH" +
            "/Uvdne3vSfaepPsS9+/SfcHeAnfHuTvM7Zvsfdfdn9N9vvsjdg+xd4e737B7vbtX6C5x9zN3P3X3b9pPsXuN+2PsjnN/tLt7vP9d9ndpX7b9" +
            "3fbu0f5X7X7R7mft7rR/o3bj9gX9G/U/0b8H999p91b7d5nbuP+mduP2Y7vfa/eL7r+m/at2v+beT9p9bPeR/Yv9N/b99D/Y/2TfX/uj7Z9r" +
            "+9u277X9be5esGvD/mvdvaDdi+5esHvN9te4f63dr7r/ivYv6H7B9sO6L7b7mN1HdH/W/pftfmrvUXeX7v7S7iN0L9L/YPeRuI9we6buU919" +
            "svvE/v/b7idxf9S9WfcRupfvvsh9mN3LtF/gfoG7i9g9X/fndf9R93Ldp9g9WfeZfSbtU+39uPvPuv+o+/P6v+v+Q91LdC+hfSn9D+p+UPun" +
            "uD/A/gD3z7kX6l7A/TnuB+heoPsTvP8I94Pcn+D+BPf93PfTvYD7fu6N7nu776N7Hvf9uK9339u9bvd63eu4X+e+D/fr3K9zX8e+jnun+9rc" +
            "17GvZd/rfV3vK7tfsFvsvuP+/W2+vS69f78396WzL/3Vbeu/18L/vZb6N7b9bX0/9P/Z+j/Y9f9vW/7vNP+/tD/z9+P/C4vF4uIXYf+FsP9C" +
            "2C8WFv8Piy8WFv8PiwsW/w+LLxYW8b9YWMyLLxbz4g8vFvN+YTEvFvO7i8W8WMyLxbzfvFjM+9XFYl5YsO1337+v9d/T7oZff767G9707vve" +
            "u4W/d/9/sVgs9or9/4vFf8P+C2ExL/5YWMyLP/5vWBywWBzUv7/mC+m+mO4Vus+le4WunO4VunK6z6Urp6uU7krpLqUrl+5KurKZ6S+lK6er" +
            "pKuYrmK6sundv33/b9/+27f79m1m++9m9+/m/N8WiwvWfn6x9f8f/g2G/xfu/8Xv/g2GP4T9F8L/AYuF737RYgH/CxcUiy8oFosW/A9csFiA" +
            "/wEXLA7oD2pfsP3Ttn+6Nf2Tbfun27X/f23b/unfD6R/X9P2/7Vt3/f2/T0gXcvCgS72D3Sx3/+A/gH2v9B/QP8X/f9p/8v+f2vbyP6Xrf9l" +
            "++9b28b73+/7f/t07e93/9/b1rf++7b1rf9e/732v/v/+L68bcu+70u3L/u+uK8b2f9m3z/fvvTvx7dv/Vvx/b99u7fvv7ZfWvsitY3pW6f9" +
            "SfvfL7Ytfb9fWvuitr9frC/U9pX8f3D/fXFto74v/7/tK+h/of//Y/v/6v+P7uP+/+N2/P+f/h+33/8XFou/Wvzv/C9YmH0h7P9h8YeF2RcC" +
            "/isAAAAASUVORK5CYII="

        // Heart outline image
        private const val SAMPLE_HEART_IMAGE =
            "iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAYAAABw4pVUAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH" +
            "6AcDExIHF7yC5gAABmFJREFUeNrtm2toVEcYx39n9+TeZDeb3fVpYgKCRvCBIogoIor4QFMFER8ogoKCIqI+EETFqFH8IIgGiQ8ERYRERCNE" +
            "EfGPoqKgoqgPQDG66U12s9ndvO/OnD4cm6S7p6fHzp6dHfsfGGZ2prrrvGfOd87MmTMCgSAQBIFAeALWn6fX9en+pfr9UnuS1v9O+/9X7f+L" +
            "9Suxf6l+T9V+j+pXbH8F9pfaH7P9p9rfof3S+v3f67NfWr/9PdpnrH1K+7T2ae3T2v8p7VPa/3e/V9v3av+D/S+x/9VpX9P9Dvs9r/1D9ofY" +
            "b/r3mU3rN63ftX6n9unW77R+63etf0/73tY/pn3G+m2yP6/9pPZj2vvaf3f9fWv/2/p3tc/M/b36b539Wdbvsf5t63dr36h9I+u3vL9S699s" +
            "6+/R9m/p91/ofz/vL9b6vdfvvdTvF96/hfcXfXmPvu79XunXvf0Krv9K7mvdX6X379v/u6zvs+pffXofWfeN7r2n6O/XU+R/Nfnfqf7X+fW/" +
            "u6X/3eyf7v7H3X8vL+7X6OfXa7g/9R6P/L0R7793FfeXub8K97Pcz3Y/230X97Pcfyv3f+7+z7r/Ue7vun8X9z/i/oe7P8n9Xvdv6v7N3O/k" +
            "/ge436b7N3G/lfvt3L+p+7f0/b93/3Xuj3Y/wv0o9z/YvYv7I+w+bvcId9909w3ux7pvev893R/mvp+7v9f9m+rP9f/Z+m8Zp22ctv69tv5T" +
            "7RvrP7H+XWvfZf0Ltn7e+vfa+q+0L9T6N9v6NfWfWP9W6/+Z9UfW77D+uPWHW/vLrv1F9YvX/77r97fPrf69bY/m/XG/R3K9Wfe91v+C6/fV" +
            "vxf997zrf+Tee4r+R/f7p/S/OveH338n96/1/nvy/jvz/lvz/t9p/wf0v+v/d9u/u3//3foH0N9Tf+/6V+Xvt0L/T8X+9zvt/4D/A8ESWBAE" +
            "gSB4NKy/Nq/X9ev8On/9df36b69fl6/7Sutv6teb6m/V93m1v+6v782b7nXnve7V7pXm9S2v703rtzfrf76sz2b1Z6tfS38l/dfXv6H+VfRf" +
            "rX559Svx77/K7//i7pfe9Z/S92/S9/2V+v6Nvr+C7y/n+8t6fa7X5zrrf7f0/df9/rLe9+e8vvNen/Osv8T6Xet3Wv/99fvdWv991m9v1v9d" +
            "1m9r1u8y9rfZ+vv9+oatv2f9vWb9fX79gda/19b/Uevfbf37W/uNf86t3/I3p9Z/K88fbfXpLfIe870X9b7uTfs3p/0XpWv9p0q9Z7rWb8L6" +
            "Tdj/EdafxvqbWb/D+v9V75ku9ZtV75ms9Xumfv0P9euYpP9V+N957zNfW+I5L++c9D7p9fKeeC8s5Z28eL9V8uL9vD8v5fW6pX9v8+855Z/C" +
            "W7/Zf+cs8SfeE/+/pvhffH+S/F33ZfH/r8Vfv8XfW+6f6r7M/VLul3Kf6p6g/UnuP8n9D7p/r/vE/vd0/8b969zvd/+q7vfZP9b9Pvt3df+K" +
            "7le4X8r9N8u+SvcruF9Bf0P6/fS/0b9B72vd99b9VfT72X7f2v3Y7t9R/wG/AEEQCILACDyw6X/tHulN8g7vR70fXevvFv/Pqv6f6/9Z9f8z" +
            "qf9/df/6+vU36m+Uq+f77r+uX/et7t9av+7b3L+Vft23uL+mX9dt7q/p13Wr+3fTr+uWfl23+Hvd0r9fKfePcr+I+wO8eF9Ivx/p/C6v/3Gk" +
            "/17sX3B/Pv9T0b/D6/+p6D/p9V9f96Xrv9b9q+s/6/XfUPeF9K9uX65fZfvy9vU89v/fWv6pbf0pbf0p6/+Ytf/p2r9m/d9bf3Ttj0r7o9Iu" +
            "rvZ/W/uTrT9m/XFrH83rv8f669YfY/3RrL9p/Xbrb9p/R/uV9l+69itp/6Vrf5O1X07t59bO0b0u6p6ndpbe69b7te6u9feW7tdS/1p6v5be" +
            "V1vva2vvK9tva+tr2fpL7b1m61etX8fWv2vrd6xdx9avWruard+wdpW1P65dsfVHrT3G2uPUHqP9UetX2Pql9n5Zvb6s3mSvrzXps1lfsj7r" +
            "q58v9T7rvmSq79F7vI/+o/vo/W7eI//T/6P/u3uL/738/0YQfD3gPxI/S2zH6VstAAAAAElFTkSuQmCC"

        // Flower outline image
        private const val SAMPLE_FLOWER_IMAGE =
            "iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAYAAABw4pVUAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH" +
            "6AcDExIHF7yC5gAABmxJREFUeNrtm2doVEsYx39nd03vbrIbu4vNGrOoiYiIgoiIImJAFAVREfH9IiAiKBgQDSIgigiKCPpBEAUREUFFEBEF" +
            "ERHRCImIIn5RVERUFPUBKEY3ucnudncvO3Pn9eHYJN09PT129uzs2P/AMLMz1V3nPXO+c2bOnBEIBIEgCASB8ECsf0+v69b9S/X7pXYnrX9N" +
            "+3+v/X+R/tvsX6zfU7Vva/86ptX+vGnvYv9K7Y/Z/jPt79B+af2z9/is9v+fNfXp/u/v+fD72v8Z9Xtt/6vTvqb7XfZ7XvuH7A9xP8z/9/K/" +
            "96n+O6f/t3L/6e6f9+7X/8uOunX+OaX8v9D/gfYv99+/d2/+n3X+H9X++vdX+X9V8G+xf9+/U1pW6dfU3Xf+M6ZfqfVttX63un+v3TveatX" +
            "+t/RvtV1v/hvV/7/0b69eZ6jdR66+6/0Wv799X0WufX8H7v/X+f/b6L/f6f/P+e9l/L+//hfb/of/f6X8/69exfhWuf1P/f9vXcX9+N/tnd" +
            "F8m/yvtZ/i/k/vvtX/L9X9t//bS//vdf7/7f9T9m9y/pftvdD/K/U/7P07vP8r9Zvdn7G6ze8zuzVz/zN0f4f7T7mbu7ub6b+b+DfcP7v4N" +
            "3L+B+9dwf9X9M7uvcv8W98e4f5v7Y9zf4P4N92+6v8v7z9rXun8z91tc/zbXp7g/yf0PuxvtbuzubO7e3N0Puxvuvp+7e9y9g3v7uf/O7ofb" +
            "vYvdt9w9zN3f6p6m179T7/fV/+4m9693v9b9Uve/6O6K93Vv1f/O3X+L969zv7bfeXv//Z7/gCAIBEHgCby36Z7tWfK2b+W9Yv0V3ivv+Vbe" +
            "K9Zfuf5K7q90v/z/6X2/+V/+N+99pv9399XU39T/m7p/S/e/7G60656u179dr3u7Xv+Pev/9ev3b9boN9dfV++8X6g31GvcLe7reUL/W7sa4" +
            "G+Purm+v776u/0N9/of6PfR76Hdf6v/Q3u+m/b67X+7uV7gfcfequ39y98vufe5v3X+U+1vtvun+Ufej3Z90N9pdL/fHut+/9/+v3N03ufuh" +
            "3Zt2f8Tuj9r9Ebtf4n6Y3S9192S//mX9f6F/A/3v698T/e/rf7N+/df/+q/XW+91rfXvtPU7Xet3uvaetv6Ntn6nfR20foftv2H9N9gft/2x" +
            "/j7Wv7f3n+29L6nfVvteUuufXf99bN8DbeunttUba+u/6fpPbf9Naf9F6dpWpWunSrdW6fKpUqVV6XpZ+6v6/YbeX6jX9+or9Pqv7PlW9nwr" +
            "9/8f77d63n+V//mifgXPf3W811dwrfXv0v27uF7M/XGuN3f3mX9Pifec8B7p9Z7t857tzfvvXfL+9/C+E7xfXbzvRP9/BvfLuL/O/Qrur/Z+" +
            "9f+n9L9F/C/N/3z8r2H9Dax/b6i/mfsN3B/neuO+/h/j/vXur9W7O/Z7uX8d99dwf6X7K7m/gvsPev+R7g/pfaX7K7o/pPeV3F/h/j/u/8P9" +
            "u7u/u/fL+XWf+3v3e7mf4v7evEfu7XvyHvn/7v8vCIJAEDwZ/AIEQSDe"
    }
}
