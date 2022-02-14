
class UniversitiesFragment : BaseFragment(), FilterUniversitiesDialogFragment.FilterUniversitiesListenerInterface {

    private lateinit var universitiesViewModel: UniversitiesViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private var _binding: FragmentUniversitiesBinding? = null
    private val binding get() = _binding!!
    lateinit var filterListenerInterface: FilterUniversitiesDialogFragment.FilterUniversitiesListenerInterface
    lateinit var filterDialog: FilterUniversitiesDialogFragment
    private var adapterUniversities = ItemAdapter<AbstractItem<*>>()
    private var fastAdapterUniversities = BindableFastAdapter<AbstractItem<*>>()
    private var isFragmentConnectToViewModel = false
    private var isFragmentOnPause = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        universitiesViewModel = UniversitiesViewModel()
        profileViewModel = ProfileViewModel()
        filterListenerInterface = this  /** listeners of BottomSheetDialogFragment */
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUniversitiesBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun init() {

        sortPopUpMenu() /** Sort filter text UI */

        addChipsFromFilter() /** set user default filter items */

        /** Show filter bottom dialog */
        binding.filterIb.setOnClickListener {
            showFilterBottomDialog()
        }

        if (!isFragmentConnectToViewModel) {
            fastAdapterUniversities.addAdapter(0, adapterUniversities)
            isFragmentConnectToViewModel = true
        }
        binding.recyclerViewUniversities.adapter = fastAdapterUniversities

        /** data from API */
           subscribe(universitiesViewModel.universities) { universities ->
                binding.loadingProgressBar.visibility = View.GONE

                if (!isFragmentOnPause) { /** BUG fix. Do not add elements if return from backstack. (fix duplicate) */
                    universities.items?.forEach { it ->
                        adapterUniversities.add(UniversityItem(it, R.layout.item_university))
                    }
                } else { /** BUG fix. Do not add elements if return from backstack. (fix duplicate) */
                    isFragmentOnPause = false
                }
                /** set load items count to filter UI */
                universitiesViewModel.userNavigation.totalCount =
                    adapterUniversities.adapterItemCount
            }

        /** Attach Click to detail fragment */
        fastAdapterUniversities.bind { vh, item ->
            if (item is UniversityItem) {
                vh.itemView.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString(Contracts.ARG_PARAM_UNIVERSITY_ID, item.data.id)
                    bundle.putString(Contracts.ARG_PARAM_UNIVERSITY_NAME, item.data.name)
                    NavTo().destination(
                        R.id.action_universitiesFragment_to_universitiesDetailFragment,
                        bundle
                    )
                }
            }
        }



        /** UI. LOAD NEXT DATA PAGES. REMOVE LOADING ITEM */
        binding.recyclerViewUniversities.addOnScrollListener(object: RecyclerView.OnScrollListener(){
             /** check listener scroll recycler view */
             override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                 super.onScrolled(recyclerView, dx, dy)
                 if(dy > 0) { //Scroll down
                     //MOCKUP if need check listener scroll recycler view
                     val layoutManager: LinearLayoutManager = binding.recyclerViewUniversities.layoutManager as LinearLayoutManager
                     if (layoutManager.findLastVisibleItemPosition()+5 > universitiesViewModel.userNavigation.nav.page *10) {
                         universitiesViewModel.userNavigation.nav.page +=1 /** NAVIGATION = NEXT PAGE */
                         universitiesViewModel.getRemoteData() /** NEW PART LOAD data from API */
                     }
                  }
                 if (dy < 0) { //Scroll up
                     //MOCKUP if need check listener scroll recycler view
                 }
             }
         })

        /** swipe screen listener */
        binding.swipeToRefresh.setOnRefreshListener {
            reloadData()
        }


        /** clear ALL filters */
        binding.clearChips.setOnClickListener {
            disableAllFilters()
        }


    }

    fun sortPopUpMenu() {
        val listPopupWindow = ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle)
        listPopupWindow.anchorView = binding.sortFilterTv
        val items = listOf(
            getString(R.string.sort_filter_text_01),
            getString(R.string.sort_filter_text_02))
        val adapter = ArrayAdapter(requireContext(), R.layout.list_popup_sort_item, items)
        listPopupWindow.setAdapter(adapter)
        listPopupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->

            binding.sortFilterTv.text = items[position]

            /** RELOAD SORTED DATA TO UI (from api) */
            when(position) {
                0 -> {
                    universitiesViewModel.userNavigation.sort.sortDest = null
                    universitiesViewModel.userNavigation.sort.sortType = null
                    reloadData()
                }
                1 -> {
                    universitiesViewModel.userNavigation.sort.sortDest = Contracts.FILTER_SORT_DESTINATION_REVERSE
                    reloadData()
                }
            }

            listPopupWindow.dismiss()
            binding.sortFilterIb.setImageResource(R.drawable.ic_round_arrow_black_down_24)
        }
        binding.sortFilterTv.setOnClickListener { v: View? ->
            listPopupWindow.show()
            binding.sortFilterIb.setImageResource(R.drawable.ic_round_arrow_black_up_24)
        }
        binding.sortFilterIb.setOnClickListener { v: View? ->
            listPopupWindow.show()
            binding.sortFilterIb.setImageResource(R.drawable.ic_round_arrow_black_up_24)
        }

    }



    private fun showFilterBottomDialog() {
        filterDialog = FilterUniversitiesDialogFragment(universitiesViewModel.userNavigation, filterListenerInterface)
        filterDialog.show(this.parentFragmentManager, "tag")


    }



    private fun addChipsFromFilter() {
        val mInflater = requireContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        /** add CHIP user ball (if != 0) */
        var sumUserValues = 0
        universitiesViewModel.userNavigation.filter.userBalls.items?.forEach {
            sumUserValues += it.value
        }

        if (sumUserValues != 0) {
            val strBallValue: String = getString(R.string._filter_chip_values_text) +
                    " " + profileViewModel.getSumValues().toString()

            val chip = mInflater.inflate(R.layout.item_chip, null) as Chip
            chip.tag = Contracts.USER_FILTER_SUBJECTS_VALUE_TAG
            chip.text = strBallValue
            chip.setOnClickListener {
                universitiesViewModel.userNavigation.filter.userBalls = SubjectsObject(mutableListOf())
                reloadData()
                binding.chipGroup.removeView(chip)
                if (binding.chipGroup.isEmpty()) {
                    setFilterOutputVisibility(false)
                }
            }
            binding.chipGroup.addView(chip, binding.chipGroup.childCount)
        }

        /** add CHIPS cities */
            universitiesViewModel.userNavigation.filter.userCities.keys.forEach { cityStr ->
                val chip = mInflater.inflate(R.layout.item_chip, null) as Chip
                chip.tag = cityStr
                chip.text = cityStr
                chip.setOnClickListener {
                    universitiesViewModel.removeCityFromNavigationFilter(cityStr)
                    reloadData()
                    binding.chipGroup.removeView(chip)
                    if (binding.chipGroup.isEmpty()) {
                       setFilterOutputVisibility(false)
                    }
                }
                binding.chipGroup.addView(chip, binding.chipGroup.childCount)
            }

        if (binding.chipGroup.childCount == 0) {
            setFilterOutputVisibility(false)
        } else {
            setFilterOutputVisibility(true)
        }
    }


    private fun reloadData() {
        adapterUniversities.clear()
        isFragmentConnectToViewModel = false
        universitiesViewModel.userNavigation.nav.page = 1
        binding.swipeToRefresh.isRefreshing = false
        universitiesViewModel.isDataEnd = false
        adapterUniversities.clear()
        universitiesViewModel.getRemoteData()
    }


    private fun disableAllFilters() {

        /** delete balls filter */
        universitiesViewModel.userNavigation.filter.userBalls = SubjectsObject(mutableListOf())

        val chipSubjectValues = binding.chipGroup.findViewWithTag<Chip>(Contracts.USER_FILTER_SUBJECTS_VALUE_TAG)
        binding.chipGroup.removeView(chipSubjectValues)

        /** delete all cities filter */
        val filterCities: MutableMap<String, String> = mutableMapOf()
        filterCities.putAll(universitiesViewModel.userNavigation.filter.userCities)

        filterCities.forEach {
            universitiesViewModel.removeCityFromNavigationFilter(it.key)
        }
        reloadData()
        binding.chipGroup.removeAllViews()

        binding.clearChips.visibility = View.GONE
        binding.horizontalChipsScroll.visibility = View.GONE
    }


    private fun setFilterOutputVisibility(isVisible: Boolean) {
        if (isVisible) {
            binding.clearChips.visibility = View.VISIBLE
            binding.horizontalChipsScroll.visibility = View.VISIBLE
        } else {
            binding.clearChips.visibility = View.GONE
            binding.horizontalChipsScroll.visibility = View.GONE
        }
    }

    /** CALLBACK from filter dialog */
    override fun onOptionClick(filteredNav: UserNavigation?, tag: String) {
        filterDialog.dismiss() /** close dialog */

        /** rebuild vm navigation filter */
        var newCitiesFilter: MutableMap<String, String> = mutableMapOf()
        filteredNav?.filter?.let { newCitiesFilter.putAll(it?.userCities) }
        universitiesViewModel.userNavigation.nav.page = 1
        universitiesViewModel.userNavigation.filter.userCities.clear()
        newCitiesFilter?.let { universitiesViewModel.userNavigation.filter.userCities.putAll(it) }
        //TODO set HERE new filter logic to view model, if need :)
        reloadData() /** reload data from api */
        binding.chipGroup.removeAllViews() /** clear chip filter from UI */
        setFilterOutputVisibility(true)
        addChipsFromFilter() /** set chip filter to UI */
    }

    override fun onPause() {
        super.onPause()
        isFragmentOnPause = true /** BUG fix. Do not add elements if return backstack. (fix duplicate) */
    }
}
