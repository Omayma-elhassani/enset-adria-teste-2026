package org.sid.transactionservice.web.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.sid.transactionservice.exception.AccountServiceUnavailableException;
import org.sid.transactionservice.exception.BusinessException;
import org.sid.transactionservice.exception.TransactionNotFoundException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        ErrorType type;
        if (ex instanceof TransactionNotFoundException) type = ErrorType.NOT_FOUND;
        else if (ex instanceof BusinessException) type = ErrorType.BAD_REQUEST;
        else if (ex instanceof AccountServiceUnavailableException) type = ErrorType.INTERNAL_ERROR;
        else return null;
        return GraphqlErrorBuilder.newError(env).errorType(type).message(ex.getMessage()).build();
    }
}
