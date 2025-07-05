// @ts-check
import starlight from "@astrojs/starlight";
import { defineConfig } from "astro/config";
import codeConstantsPlugin from "./src/utils/remark/code_const";
import { LATEST_COMMANDS_RELEASE, LATEST_MC_RELEASE, LATEST_PAPER_RELEASE } from "./src/utils/versions.js";

// https://astro.build/config
export default defineConfig({
  integrations: [
    starlight({
      title: "StrokkCommands",
      social: [{ icon: "github", label: "GitHub", href: "https://github.com/Strokkur424/StrokkCommands" }],
      sidebar: [
        {
          label: "Documentation",
          items: [{ slug: "docs" }, { slug: "docs/dependency" }, { slug: "docs/commands" }],
        },
      ],
    }),
  ],
  markdown: {
    remarkPlugins: [
      [
        codeConstantsPlugin,
        {
          constants: {
            LATEST_MC_RELEASE,
            LATEST_PAPER_RELEASE,
            LATEST_COMMANDS_RELEASE,
          },
        },
      ],
    ],
  },
});
