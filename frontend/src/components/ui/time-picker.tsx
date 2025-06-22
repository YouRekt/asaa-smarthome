"use client";

import { Label } from "@/components/ui/label";
import { TimePickerInput } from "@/lib/time-picker-input";
import * as React from "react";

interface TimePickerProps {
	date: Date | undefined;
	setDate: (date: Date | undefined) => void;
}

export function TimePicker({ date, setDate }: TimePickerProps) {
	const minuteRef = React.useRef<HTMLInputElement>(null);
	const hourRef = React.useRef<HTMLInputElement>(null);

	return (
		<div className="flex items-end gap-2 ml-1">
			<div className="grid gap-1 text-center">
				<TimePickerInput
					picker="hours"
					date={date}
					setDate={setDate}
					ref={hourRef}
					onRightFocus={() => minuteRef.current?.focus()}
				/>
				<Label htmlFor="hours" className="text-xs">
					Hours
				</Label>
			</div>
			<div className="grid gap-1 text-center">
				<TimePickerInput
					picker="minutes"
					date={date}
					setDate={setDate}
					ref={minuteRef}
					onLeftFocus={() => hourRef.current?.focus()}
				/>
				<Label htmlFor="minutes" className="text-xs">
					Minutes
				</Label>
			</div>
		</div>
	);
}
