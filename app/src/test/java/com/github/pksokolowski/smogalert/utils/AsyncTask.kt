package android.os

/**
 * This class is shadowing system's AsyncTask class in order to provide an easier to handle
 * synchronous execution of all AsyncTasks in tested classes.
 *
 * Notice that UI thread execution limitation, if any, does not apply to the testing environment.
 * Therefore for example Room isn't going to throw an exception when run synchronously in local tests.
 */
abstract class AsyncTask<Params, Progress, Result> {

    open fun onPreExecute() {}

    open abstract fun doInBackground(vararg params: Params): Result

    open fun onPostExecute(result: Result) {}

    open fun onProgressUpdate(vararg values: Progress) {}

    fun execute(vararg params: Params): AsyncTask<Params, Progress, Result> {
        onPreExecute()
        val result = doInBackground(*params)
        onPostExecute(result)
        return this
    }
}