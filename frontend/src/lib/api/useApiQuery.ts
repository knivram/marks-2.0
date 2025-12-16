import { useQuery, UseQueryOptions } from "@tanstack/react-query";
import axiosClient from "@/lib/api/axios.ts";
import { AxiosRequestConfig } from "axios";
import { useRequestErrorHandler } from "@/lib/api/useRequestErrorHandler.ts";
import { useEffect } from "react";

interface QueryConfig<Result>
  extends Omit<UseQueryOptions<Result>, "queryKey" | "queryFn"> {
  doNotProcessError?: true;
}

interface RequestConfig<Result>
  extends Omit<AxiosRequestConfig, "url" | "method"> {
  url: AxiosRequestConfig["url"];
  method: AxiosRequestConfig["method"];
  queryConfig?: QueryConfig<Result>;
}

export const useApiQuery = <Result>(requestConfig: RequestConfig<Result>) => {
  const { handleError } = useRequestErrorHandler();
  const queryResult = useQuery<Result>({
    queryKey: [requestConfig.url, requestConfig.params],
    queryFn: async () => {
      const response = await axiosClient(requestConfig);
      return response.data;
    },
    ...requestConfig.queryConfig,
  });

  useEffect(() => {
    if (queryResult.isError) {
      if (!requestConfig.queryConfig?.doNotProcessError) {
        handleError(queryResult.error);
      }
    }
  }, [
    handleError,
    queryResult.error,
    queryResult.isError,
    requestConfig.queryConfig?.doNotProcessError,
  ]);

  return queryResult;
};
