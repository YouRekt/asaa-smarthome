import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
	return twMerge(clsx(inputs));
}

export function formatDate(date: string | Date) {
	try {
		return new Intl.DateTimeFormat("pl-PL", {
			dateStyle: "short",
			timeStyle: "short",
			timeZone: "Europe/Warsaw",
		}).format(typeof date === "string" ? new Date(date) : date);
	} catch {
		return null;
	}
}
