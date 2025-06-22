import type { ConfigurationFormValues } from "@/components/config-panel/configuration-form";
import { CounterInput } from "@/components/config-panel/counter-input";
import {
	FormControl,
	FormDescription,
	FormField,
	FormItem,
	FormLabel,
	useFormField,
} from "@/components/ui/form";
import { agentTemplates } from "@/hooks/use-store";
import { cn } from "@/lib/utils";
import { useFieldArray, useFormContext } from "react-hook-form";

const ConfigurationFormAgents = ({ areaIndex }: { areaIndex: number }) => {
	const { control } = useFormContext<ConfigurationFormValues>();
	const { error } = useFormField();

	const { fields: agents } = useFieldArray({
		control: control,
		name: `areas.${areaIndex}.agents`,
	});

	return (
		<div
			className={cn(
				"grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 p-3 rounded-md border transition-colors",
				"border-input bg-transparent",
				error && "border-destructive"
			)}
		>
			{agents.map((field, agentIndex) => {
				const agentTemplate = agentTemplates.find(
					(template) => template.name === field.templateId
				);

				return (
					<FormField
						key={field.id}
						control={control}
						name={`areas.${areaIndex}.agents.${agentIndex}.count`}
						render={({ field }) => (
							<FormItem>
								<FormLabel>
									{agentTemplate
										? agentTemplate.name
										: "Unknown Agent"}
								</FormLabel>
								<FormDescription>
									{agentTemplate
										? agentTemplate.description
										: "No description available"}
								</FormDescription>
								<FormControl>
									<CounterInput
										value={field.value}
										onValueChange={field.onChange}
										min={0}
									/>
								</FormControl>
							</FormItem>
						)}
					/>
				);
			})}
		</div>
	);
};
export default ConfigurationFormAgents;
