import { useMutation, UseMutationOptions } from "@tanstack/react-query";
import { AxiosRequestConfig } from "axios";
import axiosClient from "@/lib/api/axios.ts";
import { useRequestErrorHandler } from "@/lib/api/useRequestErrorHandler.ts";
import { useEffect } from "react";

interface QueryConfig<Result, Body>
  extends Omit<
    UseMutationOptions<Result, Error, Body>,
    "mutationKey" | "mutationFn"
  > {
  doNotProcessError?: true;
}

interface RequestConfig<Result, Body>
  extends Omit<AxiosRequestConfig, "data" | "url" | "method"> {
  url: AxiosRequestConfig["url"];
  method: AxiosRequestConfig["method"];
  queryConfig?: QueryConfig<Result, Body>;
}

export const useApiMutation = <Result, Body>(
  requestConfig: RequestConfig<Result, Body>,
) => {
  const { handleError } = useRequestErrorHandler();
  const mutationResult = useMutation({
    mutationKey: [requestConfig.url, requestConfig.params],
    mutationFn: async (body: Body) => {
      const response = await axiosClient({
        ...requestConfig,
        data: body,
      });
      return response.data;
    },
    ...requestConfig.queryConfig,
  });

  useEffect(() => {
    if (mutationResult.isError) {
      if (!requestConfig.queryConfig?.doNotProcessError) {
        handleError(mutationResult.error);
      }
    }
  }, [
    mutationResult.isError,
    requestConfig.queryConfig?.doNotProcessError,
    mutationResult.error,
    handleError,
  ]);

  return mutationResult;
};
