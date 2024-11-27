package dam.clases.monje_financiero_app.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dam.clases.monje_financiero_app.R

class CategoryAmountAdapter(private val categoryExpenseMap: Map<String, Double>) : RecyclerView.Adapter<CategoryAmountAdapter.ViewHolder>() {

    // Vista que va a mostrar cada ítem del RecyclerView
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryIcon: TextView = itemView.findViewById(R.id.tvCategoryIcon)
        val categoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val categoryTotal: TextView = itemView.findViewById(R.id.tvCategoryExpense)
    }

    // Crear una nueva vista (item)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_expense, parent, false)
        return ViewHolder(view)
    }

    // Asignar los valores a las vistas de cada item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryExpenseMap.keys.toList()[position]
        val totalAmount = categoryExpenseMap[category] ?: 0.0

        // Asignar el ícono de la categoría (esto puede cambiar según tus datos)
        holder.categoryIcon.text = "💸" // Aquí puedes personalizar los íconos si lo necesitas

        // Asignar el nombre de la categoría
        holder.categoryName.text = category

        // Asignar el total de la categoría
        holder.categoryTotal.text = "$${"%.2f".format(totalAmount)}"
    }

    // Retornar el tamaño de la lista
    override fun getItemCount(): Int {
        return categoryExpenseMap.size
    }
}