
class ProgramsViewModel(universityId: String?) : BaseViewModel() {
    val programs: BehaviorSubject<ProgramObject> = BehaviorSubject.create()
    private val programsProvider = App.appComponent.getProgramsProvider()
    var userNavigation: UserNavigation = programsProvider.getUserNavigation() /** for nav and filtering from UI */
    var isDataEnd: Boolean = false /** flag of next data part from remote api */
    var universityId = universityId

    init {
        onCreate()
    }

    private fun onCreate() {
        getRemoteData()
    }

    fun getRemoteData() {
        toBackground {
            if (!isDataEnd) {
                val bodyRequest = generateBodyRequest(userNavigation)
                val universitiesCallback = programsProvider.getProgramsCallback(bodyRequest)
                universitiesCallback.enqueue(object : Callback<ProgramsResponse> {
                    override fun onResponse(
                        call: Call<ProgramsResponse>,
                        response: Response<ProgramsResponse>
                    ) {
                        if (response?.body() != null) {
                            val programObject = response.body()!!.mapToProgramObject()
                            if (!programObject.items.isNullOrEmpty()) {
                                programs.onNext(programObject)
                            } else {
                                isDataEnd = true
                            }
                        } else {

                        }
                    }

                    override fun onFailure(call: Call<ProgramsResponse>, t: Throwable) {
                    }

                })
            }
        }
    }


    private fun generateBodyRequest(userNav: UserNavigation): ProgramsRequest {
        /** Change data if add api filtering */
        var filter = ProgramsRequest.Filter(listOf(universityId))


        var sort: ProgramsRequest.Sort? = null
        if (!userNav.sort.sortDest.isNullOrEmpty()) {
            /** SORT */
            sort = ProgramsRequest.Sort(userNav.sort.sortDest)
        }

        val programsRequest = ProgramsRequest(
            filter,
            ProgramsRequest.Nav(userNav.nav.limit, userNav.nav.page),
            sort)
        return programsRequest
    }

}
