package xyz.nowaha.chengetawildlife.data.repos

class RepoResponse<T>(val responseType: ResponseType, val result: T, val source: Source) {

    enum class Source {
        LOCAL,
        REMOTE
    }

    enum class ResponseType {
        SUCCESS,
        CONNECTION_ERROR,
        UNAUTHORIZED,
        NOT_FOUND,
        EXPIRED,
        UNKNOWN_ERROR
    }

}