package com.chiko0085.testgym

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest


val supabase = createSupabaseClient(
    supabaseUrl = "https://oauqibiobmmtrfaydpfj.supabase.co",
    supabaseKey = "sb_publishable_3YBM8qcHZQSZR_b5wewL3w_z46zixHd"
) {
    install(Postgrest)
}