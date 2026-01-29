package pt.cravodeabril.movies.ui.person

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.search.SearchView
import pt.cravodeabril.movies.R
import pt.cravodeabril.movies.data.Resource
import pt.cravodeabril.movies.data.local.entity.FullPersonEntity
import pt.cravodeabril.movies.databinding.FragmentPersonListBinding
import pt.cravodeabril.movies.databinding.ItemPersonListBinding
import pt.cravodeabril.movies.utils.diffCallbackOf

class PersonListFragment : Fragment(R.layout.fragment_person_list) {
    private val adapter = ListAdapter()
    private val viewModel by viewModels<PersonListViewModel>()

    private var _binding: FragmentPersonListBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        viewModel.observePeople()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPersonListBinding.bind(view)

//        binding.search.apply {
//            show()
//            addTransitionListener { _, previousState, newState ->
//                if ((previousState == SearchView.TransitionState.SHOWN && newState == SearchView.TransitionState.HIDING) || newState == SearchView.TransitionState.HIDDEN) {
//                    findNavController().popBackStack()
//                }
//            }
//            editText.addTextChangedListener { editable -> viewModel.observePeople(editable.toString()) }
//        }

        binding.list.adapter = adapter

        viewModel.people.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> binding.loading.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.loading.visibility = View.GONE
                    adapter.submitList(state.data)
                }

                is Resource.Error -> {
                    binding.loading.visibility = View.GONE
                }
            }
        }

        binding.create.setOnClickListener {
            findNavController().navigate(PersonListFragmentDirections.actionPersonListUpsertFragmentToPersonCreateFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class ListAdapter :
        androidx.recyclerview.widget.ListAdapter<FullPersonEntity, ListAdapter.ViewHolder>(
            diffCallbackOf(idSelector = { it.person.id })
        ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemPersonListBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class ViewHolder(binding: ItemPersonListBinding) :
            RecyclerView.ViewHolder(binding.root) {
            private var person: FullPersonEntity? = null

            val name = binding.name
            val edit = binding.edit
            val delete = binding.delete

            fun bind(person: FullPersonEntity) {
                this.person = person

                name.text = this.person!!.person.name

                edit.setOnClickListener {
                    findNavController().navigate(
                        PersonListFragmentDirections.actionPersonListUpsertFragmentToPersonEditFragment(
                            this.person!!.person.id
                        )
                    )
                }

                delete.setOnClickListener {
                    AlertDialog.Builder(requireContext()).setTitle(R.string.delete)
                        .setMessage(R.string.confirm_deletion)
                        .setIcon(R.drawable.delete_forever_24px).setPositiveButton(
                            R.string.positive
                        ) { _, _ -> viewModel.deletePerson(this.person!!.person.id) }
                        .setNegativeButton(R.string.negative, null).setCancelable(false).create()
                        .show()
                }
            }
        }
    }
}