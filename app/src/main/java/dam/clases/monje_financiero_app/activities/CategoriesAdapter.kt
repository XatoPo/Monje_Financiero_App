package dam.clases.monje_financiero_app.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dam.clases.monje_financiero_app.R

class CategoriesAdapter(
    private val categories: MutableList<HomeActivity.CategoryWithExpense>
) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryIcon: TextView = view.findViewById(R.id.tvCategoryIcon)
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvCategoryExpense: TextView = view.findViewById(R.id.tvCategoryExpense)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_expense, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryWithExpense = categories[position]

        // Asignamos el emoji al TextView
        holder.tvCategoryIcon.text = categoryWithExpense.category.emoji

        // Asignamos el nombre de la categoría
        holder.tvCategoryName.text = categoryWithExpense.category.name

        // Asignamos los gastos de la categoría
        holder.tvCategoryExpense.text = if (categoryWithExpense.totalExpenses > 0) {
            String.format("S/. %.2f", categoryWithExpense.totalExpenses)
        } else {
            "Sin gastos"
        }
    }

    override fun getItemCount() = categories.size

    fun updateCategories(newCategories: List<HomeActivity.CategoryWithExpense>) {
        categories.clear()  // Limpia la lista anterior
        categories.addAll(newCategories)  // Añade las nuevas categorías con sus gastos
        notifyDataSetChanged()  // Notifica al RecyclerView que los datos han cambiado
    }


}