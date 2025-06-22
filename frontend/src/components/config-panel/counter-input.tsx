import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { Minus, Plus } from "lucide-react";
import * as React from "react";

interface CounterInputProps extends React.ComponentProps<"div"> {
	value?: number;
	onValueChange?: (value: number) => void;
	min?: number;
	max?: number;
	step?: number;
	disabled?: boolean;
	format?: (value: number) => string;
}

const CounterInput = React.forwardRef<HTMLDivElement, CounterInputProps>(
	(
		{
			className,
			value = 0,
			onValueChange,
			min = 0,
			max,
			step,
			disabled,
			format,
			...props
		},
		ref
	) => {
		const handleIncrement = () => {
			const newValue = +(value + (step || 1)).toFixed(2);
			if (max === undefined || newValue <= max) {
				onValueChange?.(newValue);
			}
		};

		const handleDecrement = () => {
			const newValue = +(value - (step || 1)).toFixed(2);
			if (newValue >= min) {
				onValueChange?.(newValue);
			}
		};

		return (
			<div
				ref={ref}
				className={cn("flex items-center gap-2", className)}
				{...props}
			>
				<Button
					type="button"
					variant="ghost"
					size="icon"
					onClick={handleDecrement}
					disabled={disabled || value <= min}
				>
					<Minus />
				</Button>
				<span className="text-center font-medium min-w-10">
					{format ? format(value) : value}
				</span>
				<Button
					type="button"
					variant="ghost"
					size="icon"
					onClick={handleIncrement}
					disabled={disabled || (max !== undefined && value >= max)}
				>
					<Plus />
				</Button>
			</div>
		);
	}
);

CounterInput.displayName = "CounterInput";

// Usage in a form:
// <FormField
//   control={form.control}
//   name="count"
//   render={({ field }) => (
//     <FormItem>
//       <FormLabel>Count</FormLabel>
//       <FormControl>
//         <CounterInput
//           value={field.value}
//           onValueChange={field.onChange}
//           min={0}
//           max={10}
//         />
//       </FormControl>
//       <FormDescription>
//         Select the number of items.
//       </FormDescription>
//       <FormMessage />
//     </FormItem>
//   )}
// />

export { CounterInput };
