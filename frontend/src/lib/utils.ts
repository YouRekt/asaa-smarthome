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

export function parseTimestamp(dateStr: string): Date {
	const [datePart, timePart] = dateStr.split(", ");
	const [day, month, year] = datePart.split(".");
	const [hour, minute] = timePart.split(":");

	return new Date(
		parseInt(year, 10),
		parseInt(month, 10) - 1,
		parseInt(day, 10),
		parseInt(hour, 10),
		parseInt(minute, 10)
	);
}
