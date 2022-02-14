fun BaseViewModel.toBackground(job: () -> Unit) {
    Observable.fromCallable(job)
        .subscribeOn(Schedulers.io())
        .subscribe( {})
        .addTo(disposable)
}


fun <T> BaseViewModel.subscribe(subject: Subject<T>, func: (items: T) -> Unit) {
    subject
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ items -> func.invoke(items) }, { e ->  //Crash.recordException(e)
             })
        .addTo(disposable)
}
