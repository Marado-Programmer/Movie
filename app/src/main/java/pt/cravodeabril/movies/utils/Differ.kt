package pt.cravodeabril.movies.utils

import androidx.recyclerview.widget.DiffUtil

fun <T, K> diffCallbackOf(
    idSelector: (T) -> K,
    contentsEqual: (old: T, new: T) -> Boolean = { old, new -> old == new }
): DiffUtil.ItemCallback<T> = object : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T & Any, newItem: T & Any): Boolean =
        idSelector(oldItem) == idSelector(newItem)


    override fun areContentsTheSame(
        oldItem: T & Any,
        newItem: T & Any
    ): Boolean = contentsEqual(oldItem, newItem)
}