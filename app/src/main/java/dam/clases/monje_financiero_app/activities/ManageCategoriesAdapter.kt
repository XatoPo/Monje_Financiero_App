package dam.clases.monje_financiero_app.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.models.Category

class ManageCategoriesAdapter(
    private val categories: List<Category>, // Lista de categorías
    private val onEditClick: (Category) -> Unit, // Acción para editar
    private val onDeleteClick: (Category) -> Unit // Acción para borrar
) : RecyclerView.Adapter<ManageCategoriesAdapter.CategoryViewHolder>() {

    // ViewHolder que representa cada elemento en la lista
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvCategoryIcon: TextView = itemView.findViewById(R.id.tvCategoryIcon)
        val btnEditCategory: ImageButton = itemView.findViewById(R.id.btnEditCategory)
        val btnDeleteCategory: ImageButton = itemView.findViewById(R.id.btnDeleteCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_category, parent, false) // Inflar el layout personalizado
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position] // Obtener la categoría actual

        // Asignar valores a los elementos del ViewHolder
        holder.tvCategoryName.text = category.name
        holder.tvCategoryIcon.text = category.iconText

        // Manejar clics en los botones de editar y borrar
        holder.btnEditCategory.setOnClickListener { onEditClick(category) }
        holder.btnDeleteCategory.setOnClickListener { onDeleteClick(category) }
    }

    override fun getItemCount(): Int {
        return categories.size // Número de elementos en la lista
    }
}
