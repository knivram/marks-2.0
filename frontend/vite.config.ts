import path from "path";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  base: "/marks/",
  server: {
    proxy: {
      "/marks/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        rewrite: (path) => path.slice(6),
        headers: {
          "x-forwarded-subject":
            "emailAddress=marvin.kramer@ergon.ch,CN=Marvin Kramer,OU=IT,O=Ergon Informatik AG,L=Zuerich,ST=Zuerich,C=CH",
          // "emailAddress=thomas.boksberger@ergon.ch,CN=Thomas Boksberger,OU=IT,O=Ergon Informatik AG,L=Zuerich,ST=Zuerich,C=CH",
          // "emailAddress=bernd.lienberger@ergon.ch,CN=Bernd Lienberger,OU=IT,O=Ergon Informatik AG,L=Zuerich,ST=Zuerich,C=CH",
        },
      },
    },
  },
});
