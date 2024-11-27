package dam.clases.monje_financiero_app.activities

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.models.Category

class CategoryAdapter(private val context: Context, private val clickListener: CategoryClickListener) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val categories: MutableList<Category> = mutableListOf()

    fun addCategory(category: Category) {
        categories.add(category)
        notifyItemInserted(categories.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
        holder.itemView.setOnClickListener {
            clickListener.onCategoryClicked(category)
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    interface CategoryClickListener {
        fun onCategoryClicked(category: Category)
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconTextView: TextView = itemView.findViewById(R.id.iconText)
        private val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryName)

        fun bind(category: Category) {
            // Configurar el TextView de icono y nombre
            iconTextView.text = category.iconText
            categoryNameTextView.text = category.name

            // Crear un GradientDrawable
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f // Radio de los bordes redondeados
                setStroke(1, Color.DKGRAY) // Color del borde
                val originalColor = Color.parseColor(category.color)
                val alpha = (0.8 * 255).toInt() // 80% opacidad
                setColor((alpha shl 24) or (originalColor and 0x00FFFFFF)) // Color de fondo con opacidad
            }

            // Aplicar el Drawable al fondo del itemView
            itemView.background = drawable

            // Cambiar el color del texto a negro
            iconTextView.setTextColor(Color.BLACK)
            categoryNameTextView.setTextColor(Color.BLACK)
        }
    }
}