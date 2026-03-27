UPDATE type_reports
SET emotion_stats = jsonb_set(
        emotion_stats,
        '{dominantEmotionCode}',
        to_jsonb('ETC'::text),
        true
                    )
WHERE emotion_stats -> 'dominantEmotionCode' IS NULL
   OR emotion_stats ->> 'dominantEmotionCode' IS NULL;