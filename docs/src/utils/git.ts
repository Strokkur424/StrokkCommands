// Borrowed from the PaperMC docs.
// https://github.com/PaperMC/docs

import { execSync } from "child_process";

export interface CommitterInfo {
  name: string;
  href: string;
}

export interface CommitInfo {
  hash: string;
  committer: CommitterInfo;
}

export const GITHUB_OPTIONS: RequestInit = process.env.GITHUB_TOKEN
  ? {
      headers: {
        Accept: "application/vnd.github+json",
        "User-Agent": "Strokkur424/StrokkCommands (https://commands.strokkur.net)",
        Authorization: `Bearer ${process.env.GITHUB_TOKEN}`,
      },
    }
  : {
      headers: {
        Accept: "application/vnd.github+json",
        "User-Agent": "Strokkur424/StrokkCommands (https://commands.strokkur.net)",
      },
    };

export const REPO = "Strokkur424/StrokkCommands";
const cache = new Map<string, CommitterInfo>();

export const getCommitInfo = async (filePath: string): Promise<CommitInfo | null> => {
  let hash: string, email: string, name: string;
  try {
    [hash, email, name] = execSync(`git log -1 --format="%H,%ae,%an" -- "${filePath}"`).toString().trim().split(",", 3);
  } catch (e) {
    return null;
  }

  const cached = cache.get(email);
  if (cached) {
    return { hash, committer: cached };
  }

  const info: CommitterInfo = { name, href: `mailto:${email}` };

  const res = await fetch(`https://api.github.com/repos/${REPO}/commits/${hash}`, GITHUB_OPTIONS);
  if (res.ok) {
    const commit = await res.json();
    info.href = commit.author.html_url;
  }

  cache.set(email, info);
  return { hash, committer: info };
};
