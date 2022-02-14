
class UIActions(context: Context) {
    val context = context

    private val callBtnText = context.getString(R.string.dialog_call_positive_btn)
    private val callMailText = context.getString(R.string.dialog_mail_positive_btn)
    private val callWebText = context.getString(R.string.dialog_web_positive_btn)



    /** Some dialogs with intent actions (CALL, MAIL, WEB...) */
    fun dialogStartExternalIntent(titleMassage: String, action: String, value: String) {
        val builder = AlertDialog.Builder(context)
        val title = TextView(context)
        title.text = titleMassage
        title.setPadding(100, 120, 100, 120)
        title.gravity = Gravity.CENTER
        title.setTextColor(context.getColor(R.color.title_text_color))
        title.textSize = 18f
        title.typeface =  ResourcesCompat.getFont(context, R.font.gilroy_semibold)
        builder.setCustomTitle(title)


        when(action) {
            /** call intent */
            CALL -> {
                builder.setPositiveButton(callBtnText){ dialog, which ->
                    call(getValidPhoneNumber(value))
                } }
            /** mail intent */
            MAIL -> {
                builder.setPositiveButton(callMailText){ dialog, which ->
                    goToMAil(value)
                } }
            /** web intent */
            WEB -> {
                builder.setPositiveButton(callWebText){ dialog, which ->
                    goToWeb(value)
                } }
        }

        builder.setNeutralButton(context.getString(R.string.dialog_contact_negative_btn)){ _, _ ->
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    /** intent phone call */
    private fun call(tel: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:$tel")
        context.startActivity(dialIntent)
    }

    /** intent web page */
    private fun goToWeb(url: String) {
        val uris = Uri.parse(url)
        val intents = Intent(Intent.ACTION_VIEW, uris)
        val b = Bundle()
        b.putBoolean("new_window", true)
        intents.putExtras(b)
        context.startActivity(intents)
    }

    /** intent def mail client */
    private fun goToMAil(mail: String) {
        val addresses: Array<String> = arrayOf(mail)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, Contracts.EXTERNAL_INTENT_MAIL_SUBJECT)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, context.getString(R.string.dialog_contact_mail_error_text), Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val CALL = "call"
        const val MAIL = "mail"
        const val WEB = "web"
    }

    private fun getValidPhoneNumber(str: String): String {
        /** Parse only first tel number in string */
        return str.substringBefore(",")
    }
}
