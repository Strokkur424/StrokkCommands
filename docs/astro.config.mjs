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
      favicon: "/patchnotes.png",
      social: [{ icon: "github", label: "GitHub", href: "https://github.com/Strokkur424/StrokkCommands" }],
      customCss: ["/src/styles/custom.css"],
      expressiveCode: {
        themes: ["dark-plus", "light-plus"],
      },
      components: {
        Footer: "/src/components/overrides/Footer.astro",
        LastUpdated: "/src/components/overrides/LastUpdated.astro",
      },
      lastUpdated: true,
      sidebar: [
        {
          label: "Documentation",
          items: [
            { slug: "docs" },
            { slug: "docs/dependency" },
            { slug: "docs/commands" },
            { slug: "docs/arguments" },
            { slug: "docs/permissions" },
          ],
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
