import com.gilt.timeuuid._

val uuid = TimeUuid()

val timestamp = uuid.toLong
println(timestamp)

val date = uuid.toDate
println(date)

println(uuid.version())
println(uuid.variant())