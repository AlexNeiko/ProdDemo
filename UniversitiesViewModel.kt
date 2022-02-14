
class UniversitiesViewModel : BaseViewModel() {

    val universities: BehaviorSubject<UniversityObject> = BehaviorSubject.create()
    private val universitiesProvider = App.appComponent.getUniversitiesProvider()
    var userNavigation: UserNavigation = universitiesProvider.getUserNavigation() /** for nav and filtering from UI */
    var isDataEnd: Boolean = false /** flag of next data part from remote api */

    init {
        onCreate()
    }

    private fun onCreate() {
        getRemoteData()
    }

    private fun generateBodyRequest(userNav: UserNavigation): UniversitiesRequest {
        /** Change data if add api filtering */
        val userCities = userNav.filter.userCities.keys
        var filter = UniversitiesRequest.Filter(userCities.toList())

        /** NULL CITY FILTER, IF USER HAS ALL CITIES !!!!!!! */
        if (userCities.contains(Contracts.DEFAULT_CITY_NAME)) {
            filter.ufCity = null
        }

        var sort: UniversitiesRequest.Sort? = null
        if (!userNav.sort.sortDest.isNullOrEmpty()) {
            /** SORT */
            sort = UniversitiesRequest.Sort(userNav.sort.sortDest)
        }

        val universitiesRequest = UniversitiesRequest(
            UniversitiesRequest.Nav(userNav.nav.limit, userNav.nav.page),
            filter,
            sort)
        return universitiesRequest
    }

    fun getRemoteData() {
        toBackground {
            if (!isDataEnd) {
                val bodyRequest = generateBodyRequest(userNavigation)
                val universitiesCallback = universitiesProvider.getUniversitiesCallback(bodyRequest)
                universitiesCallback.enqueue(object : Callback<UniversitiesResponse> {
                    override fun onResponse(
                        call: Call<UniversitiesResponse>,
                        response: Response<UniversitiesResponse>
                    ) {
                        if (response?.body() != null) {
                            val universityObject = response.body()!!.mapToUniversityObject()
                            if (!universityObject.items.isNullOrEmpty()) {
                                universities.onNext(universityObject)
                            } else {
                                isDataEnd = true
                            }
                        } else {

                        }
                    }

                    override fun onFailure(call: Call<UniversitiesResponse>, t: Throwable) {
                    }

                })
            }
        }
    }

    fun removeCityFromNavigationFilter(city: String) {
        if (userNavigation.filter.userCities.containsKey(city) && userNavigation.filter.userCities.size > 0) {
            userNavigation.filter.userCities.remove(city)
        }
        if (userNavigation.filter.userCities.isEmpty()) {
            userNavigation.filter.userCities.put(Contracts.DEFAULT_CITY_NAME, Contracts.DEFAULT_CITY_ID)
        }
    }



}
