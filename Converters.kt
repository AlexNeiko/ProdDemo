
/** Speciality Detail */
fun SpecialityDetailResponse.mapToSpecialityDetailObject(): SpecialityDetailObject {
    val studyVariants = mutableListOf<SpecialityDetailObject.StudyVariant?>()

    item?.variants.let { it ->
        it?.forEach { variant ->
            val grades: MutableList<SpecialityDetailObject.StudyVariant.Grade?>? = mutableListOf()
            variant?.grade?.forEach { gradeItem ->
                val alternative = gradeItem?.alternative.equals("true")
                grades?.add(SpecialityDetailObject.StudyVariant.Grade(
                    id = gradeItem?.id,
                    subjectName = gradeItem?.subjectName,
                    ball = gradeItem?.ball,
                    alternative))
            }

            studyVariants.add(SpecialityDetailObject.StudyVariant(
                id = variant?.id,
                variants = variant?.variants,
                grade = grades))
        }
    }

    return SpecialityDetailObject(
        id = item?.id,
        name = item?.name,
        freePlaces = item?.freePlaces,
        paidPlaces = item?.paidPlaces,
        costOfYear = item?.costOfYear,
        trainingTime = item?.trainingTime,
        institutionName = item?.institutionName,
        variants = studyVariants
    )
}


/** Program Detail */
fun ProgramDetailResponse.mapToProgramDetailObject(): ProgramDetailObject {
    val items = mutableListOf<ProgramDetailObject.Specialty>()

    item?.specialty.let { it ->
        it?.forEach {
            items.add(ProgramDetailObject.Specialty(id = it?.id, name = it?.name))
        }
    }

    return ProgramDetailObject(
        item?.id,
        item?.name,
        "",
        item?.university,
        item?.description,
        item?.level,
        items
    )
}
